package dev.nasenov.miscale2pg.service;

import dev.nasenov.miscale2pg.dto.MeasurementSync;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurementImport;
import dev.nasenov.miscale2pg.exception.MiScaleExportNotReadyException;
import dev.nasenov.miscale2pg.exception.MiScaleUnauthorizedException;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class MiScaleClientService {

  public record MiScaleExportRequest(
      String email,
      long requestTime,
      String appName,
      LocalDate startDate,
      LocalDate endDate,
      List<String> dataTypes,
      String fileFormat,
      int archiveSize) {

    public static MiScaleExportRequest from(MeasurementSync measurementSync) {
      return new MiScaleExportRequest(
          measurementSync.email(),
          System.currentTimeMillis(),
          "com.xiaomi.hm.health",
          measurementSync.startDate(),
          measurementSync.endDate(),
          List.of("BODY"),
          "ZIP",
          1024);
    }
  }

  public record MiScaleExportResponse(
      String userId, String taskId, Long generatedTime, Resource resource) {}

  public record Resource(
      String archiveUrl, List<String> dataTypes, long expireAt, String password) {}

  private final RestClient miScaleRestClient;

  private final RestClient s3RestClient;

  private final RetryTemplate miScaleRetryTemplate;

  private final MiScaleImportService miScaleImportService;

  public MiScaleMeasurementImport sync(MeasurementSync measurementSync) throws IOException {
    String task =
        miScaleRestClient
            .post()
            .uri("/users/{userId}/dataExportation", measurementSync.userId())
            .header("apptoken", measurementSync.appToken())
            .header(HttpHeaders.ACCEPT_LANGUAGE, "en-GB,en;q=0.6")
            .body(MiScaleExportRequest.from(measurementSync))
            .retrieve()
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatus.UNAUTHORIZED),
                (_, _) -> {
                  throw new MiScaleUnauthorizedException();
                })
            .toBodilessEntity()
            .getHeaders()
            .getOrEmpty(HttpHeaders.LOCATION)
            .getFirst();

    MiScaleExportResponse export =
        miScaleRetryTemplate.invoke(() -> getExport(task, measurementSync.appToken()));

    byte[] zip =
        s3RestClient
            .get()
            .uri(URI.create(export.resource.archiveUrl))
            .retrieve()
            .body(byte[].class);

    return miScaleImportService.build(zip, export.resource.password);
  }

  private MiScaleExportResponse getExport(String task, String appToken) {
    MiScaleExportResponse export =
        miScaleRestClient
            .get()
            .uri(task)
            .header("apptoken", appToken)
            .retrieve()
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatus.UNAUTHORIZED),
                (_, _) -> {
                  throw new MiScaleUnauthorizedException();
                })
            .body(MiScaleExportResponse.class);

    if (export == null
        || export.resource == null
        || export.resource.archiveUrl == null
        || export.resource.password == null) {
      throw new MiScaleExportNotReadyException();
    }

    return export;
  }
}
