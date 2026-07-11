package dev.nasenov.miscale2pg.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import dev.nasenov.miscale2pg.configuration.JacksonConfiguration;
import dev.nasenov.miscale2pg.service.MiScaleService;
import java.util.List;
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
@Import(JacksonConfiguration.class)
@DisabledInNativeImage
class MiScaleControllerTest {

  @MockitoBean MiScaleService miScaleService;

  @Autowired MockMvcTester mockMvcTester;

  @Test
  void shouldReturnBadRequestWhenNoFileIsUploaded() {
    mockMvcTester
        .post()
        .uri("/api/measurements")
        .multipart()
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(HttpStatus.BAD_REQUEST.value(), "Required part 'file' is not present.");
  }

  @Test
  void shouldReturnBadRequestWhenEmptyFileIsUploaded() {
    upload("")
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(HttpStatus.BAD_REQUEST.value(), "CSV file could not be read.");
  }

  @Test
  void shouldReturnBadRequestWhenNonCsvFileIsUploaded() {
    String text =
        """
        Lorem ipsum dolor sit amet consectetur adipiscing elit.
        Dolor sit amet consectetur adipiscing elit quisque faucibus.
        """;

    upload(text)
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(HttpStatus.BAD_REQUEST.value(), "CSV file could not be parsed.");
  }

  @Test
  void shouldReturnBadRequestWhenWrongCsvFileIsUploaded() {
    String csv =
        """
        id,name,age
        1,John Doe,28
        """;

    upload(csv)
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(HttpStatus.BAD_REQUEST.value(), "CSV file could not be parsed.");
  }

  @Test
  void shouldReturnBadRequestWhenCsvFileWithInvalidRowIsUploaded() {
    String csv =
        """
        time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
        2026-06-23 07:35:53+0000,4.8,60.0,20.9,4.0,38.0,25.0,516.0,75.0,36.0
        """;

    upload(csv)
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(
            HttpStatus.BAD_REQUEST.value(), "CSV file contains invalid measurement(s).");
  }

  @Test
  void shouldReturnOKWhenHeadersOnlyCsvFileIsUploaded() {
    String csv =
        "time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat";

    when(miScaleService.save(List.of())).thenReturn(0);

    upload(csv).hasStatus(HttpStatus.OK).body().isEmpty();
  }

  @Test
  void shouldReturnInternalServerErrorWhenDBExceptionIsThrown() {
    String csv =
        """
        time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
        2026-06-25 04:33:57+0000,67.8,180.0,20.9,14.422834,58.705936,2.9538348,1516.0,55.067486,6.0
        """;

    when(miScaleService.save(anyList())).thenThrow(DataIntegrityViolationException.class);

    upload(csv)
        .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .bodyJson()
        .convertTo(ProblemDetail.class)
        .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
        .containsExactly(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.");
  }

  private MvcTestResultAssert upload(String file) {
    return mockMvcTester
        .post()
        .uri("/api/measurements")
        .multipart()
        .file("file", file.getBytes())
        .exchange()
        .assertThat();
  }
}
