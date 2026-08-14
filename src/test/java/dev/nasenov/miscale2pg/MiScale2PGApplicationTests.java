package dev.nasenov.miscale2pg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.nasenov.miscale2pg.dto.MeasurementResponse;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurementCsv;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurementImport;
import dev.nasenov.miscale2pg.model.Measurement;
import dev.nasenov.miscale2pg.repository.MeasurementRepository;
import dev.nasenov.miscale2pg.service.MeasurementService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MiScale2PGApplicationTests {

  @Container @ServiceConnection
  static PostgreSQLContainer postgreSQLContainer =
      new PostgreSQLContainer(DockerImageName.parse("postgres:18.6"));

  @Autowired RestTestClient restTestClient;

  @Autowired MeasurementService measurementService;

  @Autowired MeasurementRepository measurementRepository;

  @Autowired JdbcClient jdbcClient;

  @AfterEach
  void tearDown() {
    jdbcClient.sql("TRUNCATE TABLE measurement RESTART IDENTITY CASCADE").update();
  }

  @Test
  void contextLoads() {}

  @Test
  void shouldSaveMeasurementsWhenPasswordProtectedZipIsUploaded() throws IOException {
    String completeMeasurementCsv =
        """
        time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
        2026-08-09 05:26:04+0000,66.95,180.0,20.6,14.148586,58.89407,2.9257855,1494,54.551735,6.0
        """;

    String partialMeasurementCsv =
        """
        time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
        2026-08-10 04:33:57+0000,68.2,180.0,21.0,null,null,null,null,null,null
        """;

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    String password = "password";

    try (ZipOutputStream zipOutputStream =
        new ZipOutputStream(outputStream, password.toCharArray())) {

      ZipParameters zipParameters = new ZipParameters();
      zipParameters.setEncryptFiles(true);
      zipParameters.setEncryptionMethod(EncryptionMethod.AES);
      zipParameters.setFileNameInZip("BODY/BODY_1786265904006.csv");

      zipOutputStream.putNextEntry(zipParameters);
      zipOutputStream.write(completeMeasurementCsv.getBytes());
      zipOutputStream.closeEntry();

      zipParameters.setFileNameInZip("BODY/BODY_1786265904007.csv");

      zipOutputStream.putNextEntry(zipParameters);
      zipOutputStream.write(partialMeasurementCsv.getBytes());
      zipOutputStream.closeEntry();
    }

    upload(outputStream.toByteArray(), password).expectStatus().isCreated().expectBody().isEmpty();

    Measurement completeMeasurement =
        Measurement.builder()
            .time(OffsetDateTime.parse("2026-08-09T05:26:04Z"))
            .weight(66.95)
            .height(180.0)
            .bmi(20.6)
            .fatRate(14.15)
            .bodyWaterRate(58.89)
            .boneMass(2.93)
            .metabolism(1494.0)
            .muscleRate(54.55)
            .visceralFat(6.0)
            .build();

    Measurement partialMeasurement =
        Measurement.builder()
            .time(OffsetDateTime.parse("2026-08-10T04:33:57Z"))
            .weight(68.2)
            .height(180.0)
            .bmi(21.0)
            .build();

    Stream.of(completeMeasurement, partialMeasurement)
        .forEach(
            measurement ->
                assertThat(measurementRepository.findById(measurement.time()))
                    .isPresent()
                    .get()
                    .usingRecursiveComparison()
                    .isEqualTo(measurement));
  }

  @Test
  void shouldSaveCompleteAndPartialMeasurementsWhenCsvIsUploaded() {
    String csv =
        """
        time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
        2026-06-23 07:35:53+0000,67.8,180.0,20.9,14.422834,58.705936,2.9538348,1516.0,55.067486,6.0
        2026-06-24 04:33:57+0000,68.2,180.0,21.0,null,null,null,null,null,null
        """;

    upload(csv).expectStatus().isCreated().expectBody().isEmpty();

    Measurement completeMeasurement =
        Measurement.builder()
            .time(OffsetDateTime.parse("2026-06-23T07:35:53Z"))
            .weight(67.8)
            .height(180.0)
            .bmi(20.9)
            .fatRate(14.42)
            .bodyWaterRate(58.71)
            .boneMass(2.95)
            .metabolism(1516.0)
            .muscleRate(55.07)
            .visceralFat(6.0)
            .build();

    Measurement partialMeasurement =
        Measurement.builder()
            .time(OffsetDateTime.parse("2026-06-24T04:33:57Z"))
            .weight(68.2)
            .height(180.0)
            .bmi(21.0)
            .build();

    Stream.of(completeMeasurement, partialMeasurement)
        .forEach(
            measurement ->
                assertThat(measurementRepository.findById(measurement.time()))
                    .isPresent()
                    .get()
                    .usingRecursiveComparison()
                    .isEqualTo(measurement));
  }

  @Test
  void shouldSaveMeasurementsWhenCsvWithDuplicatesIsUploaded() {
    String csv =
        """
        time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
        2026-06-25 04:33:57+0000,68.2,180.0,21.0,null,null,null,null,null,null
        2026-06-25 04:33:57+0000,67.8,180.0,20.9,14.422834,58.705936,2.9538348,1516.0,55.067486,6.0
        """;

    upload(csv).expectStatus().isCreated().expectBody().isEmpty();

    Measurement measurement =
        Measurement.builder()
            .time(OffsetDateTime.parse("2026-06-25T04:33:57Z"))
            .weight(67.8)
            .height(180.0)
            .bmi(20.9)
            .fatRate(14.42)
            .bodyWaterRate(58.71)
            .boneMass(2.95)
            .metabolism(1516.0)
            .muscleRate(55.07)
            .visceralFat(6.0)
            .build();

    assertThat(measurementRepository.findById(measurement.time()))
        .isPresent()
        .get()
        .usingRecursiveComparison()
        .isEqualTo(measurement);
  }

  @Test
  void shouldNotSaveAnyMeasurementsWhenOneMeasurementSaveFails() {
    MiScaleMeasurement valid =
        MiScaleMeasurement.builder()
            .time(OffsetDateTime.now())
            .weight(67.8)
            .height(180.0)
            .bmi(20.9)
            .fatRate(14.422834)
            .bodyWaterRate(58.705936)
            .boneMass(2.9538348)
            .metabolism(1516.0)
            .muscleRate(55.067486)
            .visceralFat(6.0)
            .build();

    MiScaleMeasurement invalid = MiScaleMeasurement.builder().time(null).build();

    List<MiScaleMeasurement> miScaleMeasurements = List.of(valid, invalid);

    assertThatThrownBy(
            () ->
                measurementService.save(
                    MiScaleMeasurementImport.of(
                        List.of(MiScaleMeasurementCsv.of(miScaleMeasurements)))))
        .isInstanceOf(DataIntegrityViolationException.class);
    assertThat(measurementRepository.findById(valid.time())).isNotPresent();
  }

  @Test
  void shouldReturnContentTooLargeWhenCsvFileGreaterThanMaxFileSizeIsUploaded() {
    String csv = "a".repeat(2 * 1024 * 1024); // 2 MiB

    upload(csv)
        .expectStatus()
        .isEqualTo(HttpStatus.CONTENT_TOO_LARGE)
        .expectBody(ProblemDetail.class)
        .value(
            response -> {
              assertThat(response).isNotNull();
              assertThat(response.getStatus()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE.value());
              assertThat(response.getDetail()).isEqualTo("Maximum upload size exceeded");
            });
  }

  @Test
  void shouldReturnOnlyMeasurementsBetweenTheTimeRange() {
    String csv =
        """
      time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
      2026-06-29 07:15:42+0000,66.8,180,20.6,13.93635,59.039665,2.9264562,1501,54.564064,6
      2026-06-30 04:34:46+0000,68.75,180,21.2,14.906007,58.374477,2.9786344,1530,55.523487,7
      2026-07-04 07:20:14+0000,68.35,180,21,15.022793,58.294365,2.9569604,1515,55.124958,7
      2026-07-05 07:34:47+0000,68.1,180,21,14.918178,58.36613,2.9496775,1511,54.991043,7
      """;

    upload(csv).expectStatus().isCreated().expectBody().isEmpty();

    MeasurementResponse expected =
        MeasurementResponse.builder()
            .time(OffsetDateTime.parse("2026-06-30T04:34:46Z"))
            .weight(68.75)
            .height(180.0)
            .bmi(21.2)
            .fatRate(14.91)
            .bodyWaterRate(58.37)
            .boneMass(2.98)
            .metabolism(1530.0)
            .muscleRate(55.52)
            .visceralFat(7.0)
            .build();

    restTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/api/measurements")
                    .queryParam("from", OffsetDateTime.parse("2026-06-29T12:00:00Z"))
                    .queryParam("to", OffsetDateTime.parse("2026-06-30T12:00:00Z"))
                    .build())
        .exchangeSuccessfully()
        .expectStatus()
        .isOk()
        .expectBody(new ParameterizedTypeReference<List<MeasurementResponse>>() {})
        .consumeWith(
            result -> {
              List<MeasurementResponse> measurements = result.getResponseBody();

              assertThat(measurements).hasSize(1);
              assertThat(measurements.getFirst()).isEqualTo(expected);
            });
  }

  @Test
  void shouldReturnMeasurementsFromLast30DaysWhenNoTimeRangeIsProvided() {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXX");
    OffsetDateTime recent =
        OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).truncatedTo(ChronoUnit.SECONDS);
    OffsetDateTime old =
        OffsetDateTime.now(ZoneOffset.UTC).minusDays(40).truncatedTo(ChronoUnit.SECONDS);
    String csv =
        """
        time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
        %s,68.75,180,21.2,14.906007,58.374477,2.9786344,1530,55.523487,7
        %s,66.8,180,20.6,13.93635,59.039665,2.9264562,1501,54.564064,6
        """
            .formatted(dateTimeFormatter.format(recent), dateTimeFormatter.format(old));

    upload(csv).expectStatus().isCreated().expectBody().isEmpty();

    MeasurementResponse expected =
        MeasurementResponse.builder()
            .time(recent)
            .weight(68.75)
            .height(180.0)
            .bmi(21.2)
            .fatRate(14.91)
            .bodyWaterRate(58.37)
            .boneMass(2.98)
            .metabolism(1530.0)
            .muscleRate(55.52)
            .visceralFat(7.0)
            .build();

    restTestClient
        .get()
        .uri("/api/measurements")
        .exchangeSuccessfully()
        .expectStatus()
        .isOk()
        .expectBody(new ParameterizedTypeReference<List<MeasurementResponse>>() {})
        .consumeWith(
            result -> {
              List<MeasurementResponse> measurements = result.getResponseBody();

              assertThat(measurements).hasSize(1);
              assertThat(measurements.getFirst()).isEqualTo(expected);
            });
  }

  private RestTestClient.ResponseSpec upload(String csv) {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new ByteArrayResource(csv.getBytes()) {
          @Override
          public String getFilename() {
            return "measurements.csv";
          }
        });

    return upload(body);
  }

  private RestTestClient.ResponseSpec upload(byte[] zip, String password) {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new ByteArrayResource(zip) {
          @Override
          public String getFilename() {
            return "7015641973_1786265904066.zip";
          }
        });
    body.add("password", password);

    return upload(body);
  }

  private RestTestClient.ResponseSpec upload(MultiValueMap<String, Object> body) {
    return restTestClient.post().uri("/api/measurements").body(body).exchange();
  }
}
