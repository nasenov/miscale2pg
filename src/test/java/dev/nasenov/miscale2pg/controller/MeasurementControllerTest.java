package dev.nasenov.miscale2pg.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import dev.nasenov.miscale2pg.service.MeasurementService;
import dev.nasenov.miscale2pg.service.ZeppLifeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest
@DisabledInNativeImage
class MeasurementControllerTest {

  @MockitoBean MeasurementService measurementService;

  @MockitoBean ZeppLifeService zeppLifeService;

  @Autowired MockMvcTester mockMvcTester;

  @Test
  void shouldReturnInternalServerErrorWhenDBExceptionIsThrown() {
    doThrow(DataIntegrityViolationException.class).when(zeppLifeService).sync(any());

    mockMvcTester
        .post()
        .uri("/api/measurements/sync?from=2026-06-20&to=2026-06-27")
        .exchange()
        .assertThat()
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
}
