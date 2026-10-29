package dev.nasenov.miscale2pg.service;

import dev.nasenov.miscale2pg.configuration.ZeppLifeConfiguration.ZeppLifeProperties;
import dev.nasenov.miscale2pg.dto.MeasurementSync;
import dev.nasenov.miscale2pg.dto.MeasurementViolation;
import dev.nasenov.miscale2pg.dto.ZeppLifeMeasurement;
import dev.nasenov.miscale2pg.dto.ZeppLifeMeasurementCsv;
import dev.nasenov.miscale2pg.dto.ZeppLifeMeasurementExport;
import dev.nasenov.miscale2pg.exception.ZeppLifeExportInvalidException;
import dev.nasenov.miscale2pg.exception.ZeppLifeExportNotReadyException;
import dev.nasenov.miscale2pg.exception.ZeppLifeUnauthorizedException;
import jakarta.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectReader;

@Service
@RequiredArgsConstructor
public class ZeppLifeService {

  public record ZeppLifeExportRequest(
      String email,
      long requestTime,
      String appName,
      LocalDate startDate,
      LocalDate endDate,
      List<String> dataTypes,
      String fileFormat,
      int archiveSize) {

    public static ZeppLifeExportRequest from(MeasurementSync measurementSync, String email) {
      return new ZeppLifeExportRequest(
          email,
          System.currentTimeMillis(),
          "com.xiaomi.hm.health",
          measurementSync.from(),
          measurementSync.to(),
          List.of("BODY"),
          "ZIP",
          1024);
    }
  }

  public record ZeppLifeExportResponse(
      String userId, String taskId, Long generatedTime, Resource resource) {}

  public record Resource(
      String archiveUrl, List<String> dataTypes, long expireAt, String password) {}

  private final RestClient zeppLifeRestClient;

  private final ZeppLifeProperties zeppLifeProperties;

  private final RetryTemplate zeppLifeRetryTemplate;

  private final RestClient s3RestClient;

  private final ObjectReader zeppLifeMeasurementReader;

  private final Validator validator;

  private final MeasurementService measurementService;

  public void sync(MeasurementSync measurementSync) {
    String task =
        zeppLifeRestClient
            .post()
            .uri("/users/{userId}/dataExportation", zeppLifeProperties.userId())
            .header("apptoken", zeppLifeProperties.appToken())
            .header(HttpHeaders.ACCEPT_LANGUAGE, "en-GB,en;q=0.6")
            .body(ZeppLifeExportRequest.from(measurementSync, zeppLifeProperties.email()))
            .retrieve()
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatus.UNAUTHORIZED),
                (_, _) -> {
                  throw new ZeppLifeUnauthorizedException();
                })
            .toBodilessEntity()
            .getHeaders()
            .getOrEmpty(HttpHeaders.LOCATION)
            .getFirst();

    ZeppLifeExportResponse export = zeppLifeRetryTemplate.invoke(() -> getExport(task));

    byte[] zip =
        s3RestClient
            .get()
            .uri(URI.create(export.resource.archiveUrl))
            .retrieve()
            .body(byte[].class);

    ZeppLifeMeasurementExport measurementsExport = readExport(zip, export.resource.password);

    List<MeasurementViolation> violations =
        validator.validate(measurementsExport).stream().map(MeasurementViolation::from).toList();

    if (!violations.isEmpty()) {
      throw new ZeppLifeExportInvalidException(violations);
    }

    measurementService.save(measurementsExport);
  }

  private ZeppLifeExportResponse getExport(String task) {
    ZeppLifeExportResponse export =
        zeppLifeRestClient
            .get()
            .uri(task)
            .header("apptoken", zeppLifeProperties.appToken())
            .retrieve()
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatus.UNAUTHORIZED),
                (_, _) -> {
                  throw new ZeppLifeUnauthorizedException();
                })
            .body(ZeppLifeExportResponse.class);

    if (export == null
        || export.resource == null
        || export.resource.archiveUrl == null
        || export.resource.password == null) {
      throw new ZeppLifeExportNotReadyException();
    }

    return export;
  }

  private ZeppLifeMeasurementExport readExport(byte[] zip, String password) {
    List<ZeppLifeMeasurementCsv> csvs = new ArrayList<>();

    try (ZipInputStream zipInputStream =
        new ZipInputStream(new ByteArrayInputStream(zip), password.toCharArray())) {
      LocalFileHeader entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (isZeppLifeMeasurementCsv(entry)) {
          try (MappingIterator<ZeppLifeMeasurement> iterator =
              zeppLifeMeasurementReader.readValues(zipInputStream.readAllBytes())) {
            csvs.add(ZeppLifeMeasurementCsv.of(iterator.readAll()));
          }
        }
      }
    } catch (IOException | JacksonException ex) {
      throw new ZeppLifeExportInvalidException(ex);
    }

    return ZeppLifeMeasurementExport.of(csvs);
  }

  private boolean isZeppLifeMeasurementCsv(LocalFileHeader entry) {
    return !entry.isDirectory()
        && entry.getFileName().startsWith("BODY/")
        && entry.getFileName().endsWith(".csv");
  }
}
