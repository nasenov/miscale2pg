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
import dev.nasenov.miscale2pg.util.ZipFileUtils;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mockserver.MockServerContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MiScale2PGApplicationTests {

  @Container @ServiceConnection
  static PostgreSQLContainer postgreSQLContainer =
      new PostgreSQLContainer(DockerImageName.parse("postgres:18.6"));

  @Container
  static MockServerContainer mockServerContainer =
      new MockServerContainer(
          DockerImageName.parse("mockserver/mockserver")
              .withTag(
                  "mockserver-" + MockServerClient.class.getPackage().getImplementationVersion()));

  @DynamicPropertySource
  static void mockServerProperties(DynamicPropertyRegistry registry) {
    registry.add("app.miscale.api.base-url", mockServerContainer::getEndpoint);
  }

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
  @DisabledInNativeImage
  void shouldSyncMeasurementsFromZeppLifeApp() throws IOException {
    String csv =
        """
        time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
        2026-08-21 05:26:04+0000,66.95,180.0,20.6,14.148586,58.89407,2.9257855,1494,54.551735,6.0
        2026-08-22 04:33:57+0000,68.2,180.0,21.0,null,null,null,null,null,null
        """;
    String zipPath = "/test.zip";
    byte[] zipFile = ZipFileUtils.create(csv);

    long userId = 1L;
    String appToken = "token";
    String taskId = "143892c5-c60c-47b4-8d70-f84cffda1bf1";
    String taskPath = "/tasks/" + taskId;

    try (MockServerClient mockServerClient =
        new MockServerClient(mockServerContainer.getHost(), mockServerContainer.getServerPort())) {

      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod(HttpMethod.POST.name())
                  .withPath("/users/" + userId + "/dataExportation")
                  .withHeader("apptoken", appToken)
                  .withHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-GB,en;q=0.6"))
          .respond(
              HttpResponse.response()
                  .withStatusCode(HttpStatus.ACCEPTED.value())
                  .withHeader(HttpHeaders.LOCATION, taskPath));

      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod(HttpMethod.GET.name())
                  .withPath(taskPath)
                  .withHeader("apptoken", appToken),
              Times.once())
          .respond(
              HttpResponse.response()
                  .withStatusCode(HttpStatus.OK.value())
                  .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                  .withBody(
                      """
                      {
                        "userId": "%s",
                        "taskId": "%s",
                        "status": 1,
                        "updateTime": 1787505133212,
                        "createTime": 1787505132969
                      }
                      """
                          .formatted(userId, taskId)));

      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod(HttpMethod.GET.name())
                  .withPath(taskPath)
                  .withHeader("apptoken", appToken),
              Times.unlimited())
          .respond(
              HttpResponse.response()
                  .withStatusCode(HttpStatus.OK.value())
                  .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                  .withBody(
                      """
                      {
                        "userId": "%s",
                        "taskId": "%s",
                        "generatedTime": 1787505133223,
                        "resource": {
                          "archiveUrl": "%s",
                          "dataTypes": [
                            "BODY"
                          ],
                          "expireAt": 86400000,
                          "password": "%s"
                        }
                      }
                      """
                          .formatted(
                              userId,
                              taskId,
                              mockServerContainer.getEndpoint() + zipPath,
                              ZipFileUtils.PASSWORD)));

      mockServerClient
          .when(HttpRequest.request().withMethod(HttpMethod.GET.name()).withPath(zipPath))
          .respond(HttpResponse.response().withStatusCode(HttpStatus.OK.value()).withBody(zipFile));

      restTestClient
          .post()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/api/measurements/sync")
                      .queryParam("userId", userId)
                      .queryParam("email", "john@example.com")
                      .queryParam("startDate", "2026-08-16")
                      .queryParam("endDate", "2026-08-23")
                      .build())
          .header("App-Token", appToken)
          .exchange()
          .expectStatus()
          .isCreated()
          .expectBody()
          .isEmpty();

      Measurement completeMeasurement =
          Measurement.builder()
              .time(OffsetDateTime.parse("2026-08-21T05:26:04Z"))
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
              .time(OffsetDateTime.parse("2026-08-22T04:33:57Z"))
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

      mockServerClient.reset();
    }
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
  void shouldReturnOnlyMeasurementsBetweenTheTimeRange() {
    List<MiScaleMeasurement> miScaleMeasurements =
        List.of(
            MiScaleMeasurement.builder()
                .time(OffsetDateTime.parse("2026-06-29T07:15:42Z"))
                .weight(66.8)
                .height(180.0)
                .bmi(20.6)
                .fatRate(13.93635)
                .bodyWaterRate(59.039665)
                .boneMass(2.9264562)
                .metabolism(1501.0)
                .muscleRate(54.564064)
                .visceralFat(6.0)
                .build(),
            MiScaleMeasurement.builder()
                .time(OffsetDateTime.parse("2026-06-30T04:34:46Z"))
                .weight(68.75)
                .height(180.0)
                .bmi(21.2)
                .fatRate(14.906007)
                .bodyWaterRate(58.374477)
                .boneMass(2.9786344)
                .metabolism(1530.0)
                .muscleRate(55.523487)
                .visceralFat(7.0)
                .build(),
            MiScaleMeasurement.builder()
                .time(OffsetDateTime.parse("2026-07-04T07:20:14Z"))
                .weight(68.35)
                .height(180.0)
                .bmi(21.0)
                .fatRate(15.022793)
                .bodyWaterRate(58.294365)
                .boneMass(2.9569604)
                .metabolism(1515.0)
                .muscleRate(55.124958)
                .visceralFat(7.0)
                .build(),
            MiScaleMeasurement.builder()
                .time(OffsetDateTime.parse("2026-07-05T07:34:47Z"))
                .weight(68.1)
                .height(180.0)
                .bmi(21.0)
                .fatRate(14.918178)
                .bodyWaterRate(58.36613)
                .boneMass(2.9496775)
                .metabolism(1511.0)
                .muscleRate(54.991043)
                .visceralFat(7.0)
                .build());

    measurementService.save(
        MiScaleMeasurementImport.of(List.of(MiScaleMeasurementCsv.of(miScaleMeasurements))));

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
    OffsetDateTime recent =
        OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).truncatedTo(ChronoUnit.SECONDS);
    OffsetDateTime old =
        OffsetDateTime.now(ZoneOffset.UTC).minusDays(40).truncatedTo(ChronoUnit.SECONDS);
    List<MiScaleMeasurement> miScaleMeasurements =
        List.of(
            MiScaleMeasurement.builder()
                .time(recent)
                .weight(68.75)
                .height(180.0)
                .bmi(21.2)
                .fatRate(14.906007)
                .bodyWaterRate(58.374477)
                .boneMass(2.9786344)
                .metabolism(1530.0)
                .muscleRate(55.523487)
                .visceralFat(7.0)
                .build(),
            MiScaleMeasurement.builder()
                .time(old)
                .weight(66.8)
                .height(180.0)
                .bmi(20.6)
                .fatRate(13.93635)
                .bodyWaterRate(59.039665)
                .boneMass(2.9264562)
                .metabolism(1501.0)
                .muscleRate(54.564064)
                .visceralFat(6.0)
                .build());

    measurementService.save(
        MiScaleMeasurementImport.of(List.of(MiScaleMeasurementCsv.of(miScaleMeasurements))));

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
}
