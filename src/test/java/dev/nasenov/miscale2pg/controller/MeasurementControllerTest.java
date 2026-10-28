package dev.nasenov.miscale2pg.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import dev.nasenov.miscale2pg.configuration.JacksonConfiguration;
import dev.nasenov.miscale2pg.service.MeasurementService;
import dev.nasenov.miscale2pg.service.MiScaleClientService;
import dev.nasenov.miscale2pg.service.MiScaleImportService;
import dev.nasenov.miscale2pg.util.ZipFileUtils;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

@WebMvcTest
@Import({JacksonConfiguration.class, MiScaleImportService.class})
@DisabledInNativeImage
class MeasurementControllerTest {

  @MockitoBean MeasurementService measurementService;

  @MockitoBean MiScaleClientService miScaleClientService;

  @Autowired MiScaleImportService miScaleImportService;

  @Autowired MockMvcTester mockMvcTester;

  @Test
  void shouldReturnInternalServerErrorWhenEmptyCsvFileIsSynced() throws IOException {
    sync(ZipFileUtils.create(""), ZipFileUtils.PASSWORD)
        .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.");
  }

  @Test
  void shouldReturnInternalServerErrorWhenNonCsvFileIsSynced() throws IOException {
    String text =
        """
        Lorem ipsum dolor sit amet consectetur adipiscing elit.
        Dolor sit amet consectetur adipiscing elit quisque faucibus.
        """;

    sync(ZipFileUtils.create(text), ZipFileUtils.PASSWORD)
        .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.");
  }

  @Test
  void shouldReturnInternalServerErrorWhenWrongCsvFileIsSynced() throws IOException {
    String csv =
        """
        id,name,age
        1,John Doe,28
        """;

    sync(ZipFileUtils.create(csv), ZipFileUtils.PASSWORD)
        .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.");
  }

  @Test
  void shouldReturnInternalServerErrorWhenCsvFileWithInvalidRowIsSynced() throws IOException {
    String csv =
        """
        time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
        2026-06-23 07:35:53+0000,4.8,60.0,20.9,4.0,38.0,25.0,516.0,75.0,36.0
        """;

    sync(ZipFileUtils.create(csv), ZipFileUtils.PASSWORD)
        .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.");
  }

  @Test
  void shouldReturnInternalServerErrorWhenHeadersOnlyCsvFileIsSynced() throws IOException {
    String csv =
        "time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat";

    sync(ZipFileUtils.create(csv), ZipFileUtils.PASSWORD)
        .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.");
  }

  @Test
  void shouldReturnInternalServerErrorWhenZipFileWithoutCsvFilesIsSynced() throws IOException {
    sync(ZipFileUtils.create(), ZipFileUtils.PASSWORD)
        .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.");
  }

  @Test
  void shouldReturnInternalServerErrorWhenZipFilePasswordIsIncorrect() throws IOException {
    String csv =
        """
        time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
        2026-06-25 04:33:57+0000,67.8,180.0,20.9,14.422834,58.705936,2.9538348,1516.0,55.067486,6.0
        """;

    sync(ZipFileUtils.create(csv), "wrong")
        .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.");
  }

  @Test
  void shouldReturnInternalServerErrorWhenDBExceptionIsThrown() throws IOException {
    String csv =
        """
        time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
        2026-06-25 04:33:57+0000,67.8,180.0,20.9,14.422834,58.705936,2.9538348,1516.0,55.067486,6.0
        """;

    doThrow(DataIntegrityViolationException.class).when(measurementService).save(any());

    sync(ZipFileUtils.create(csv), ZipFileUtils.PASSWORD)
        .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.");
  }

  @Test
  void shouldReturnBadRequestWhenFromIsNotBeforeTo() {
    mockMvcTester
        .get()
        .uri("/api/measurements?from=2026-07-30T00:00:00Z&to=2026-07-01T00:00:00Z")
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(HttpStatus.BAD_REQUEST.value(), "Invalid measurement time range.");
  }

  private MvcTestResultAssert sync(byte[] zip, String password) throws IOException {
    when(miScaleClientService.sync(any()))
        .thenAnswer(_ -> miScaleImportService.build(zip, password));

    return mockMvcTester
        .post()
        .uri(
            "/api/measurements/sync?userId=1&email=john@example.com"
                + "&startDate=2026-06-20&endDate=2026-06-27")
        .header("App-Token", "token")
        .exchange()
        .assertThat();
  }
}
