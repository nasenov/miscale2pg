package dev.nasenov.miscale2pg.controller;

import dev.nasenov.miscale2pg.dto.MeasurementSync;
import dev.nasenov.miscale2pg.dto.MeasurementTimeRange;
import dev.nasenov.miscale2pg.dto.MeasurementViolation;
import dev.nasenov.miscale2pg.exception.ZeppLifeException;
import dev.nasenov.miscale2pg.service.MeasurementService;
import dev.nasenov.miscale2pg.service.ZeppLifeService;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Slf4j
@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementController {

  private static final String INTERNAL_SERVER_ERROR_DETAIL =
      "An unexpected error occurred. Please try again later.";

  private final Validator validator;

  private final MeasurementService measurementService;

  private final ZeppLifeService zeppLifeService;

  @GetMapping
  public ResponseEntity<?> findByTimeRange(
      @RequestParam Optional<OffsetDateTime> from, @RequestParam Optional<OffsetDateTime> to) {
    MeasurementTimeRange timeRange = buildMeasurementTimeRange(from, to);

    List<MeasurementViolation> violations =
        validator.validate(timeRange).stream().map(MeasurementViolation::from).toList();

    if (!violations.isEmpty()) {
      return buildMeasurementViolationsResponse("Invalid measurement time range.", violations);
    }

    return ResponseEntity.ok(measurementService.findByTimeRange(timeRange));
  }

  @PostMapping("/sync")
  public ResponseEntity<?> sync(
      @RequestParam Optional<LocalDate> from, @RequestParam Optional<LocalDate> to) {
    MeasurementSync measurementSync = buildMeasurementSync(from, to);

    List<MeasurementViolation> violations =
        validator.validate(measurementSync).stream().map(MeasurementViolation::from).toList();

    if (!violations.isEmpty()) {
      return buildMeasurementViolationsResponse("Invalid measurement sync request.", violations);
    }

    zeppLifeService.sync(measurementSync);

    return ResponseEntity.ok().build();
  }

  @ExceptionHandler(ZeppLifeException.class)
  public ProblemDetail handleZeppLifeException(ZeppLifeException ex) {
    log.error("Zepp Life exception", ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_DETAIL);
  }

  @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
  public ProblemDetail handleHttpClientErrorExceptionTooManyRequests(
      HttpClientErrorException.TooManyRequests ex) {
    log.error("Too many requests", ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.TOO_MANY_REQUESTS, "Too many requests were sent. Please try again later.");
  }

  @ExceptionHandler(RestClientException.class)
  public ProblemDetail handleRestClientException(RestClientException ex) {
    log.error("Rest client exception", ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_DETAIL);
  }

  @ExceptionHandler(DataAccessException.class)
  public ProblemDetail handleDataAccessException(DataAccessException ex) {
    log.error("Database exception", ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_DETAIL);
  }

  private MeasurementTimeRange buildMeasurementTimeRange(
      Optional<OffsetDateTime> from, Optional<OffsetDateTime> to) {
    OffsetDateTime effectiveTo =
        to.orElseGet(
            () ->
                from.map(effectiveFrom -> effectiveFrom.plusDays(30))
                    .orElseGet(() -> OffsetDateTime.now(ZoneOffset.UTC)));
    OffsetDateTime effectiveFrom = from.orElseGet(() -> effectiveTo.minusDays(30));

    return MeasurementTimeRange.of(effectiveFrom, effectiveTo);
  }

  private MeasurementSync buildMeasurementSync(Optional<LocalDate> from, Optional<LocalDate> to) {
    LocalDate effectiveTo = to.orElseGet(() -> LocalDate.now(ZoneOffset.UTC));
    LocalDate effectiveFrom =
        from.orElseGet(
            () ->
                measurementService
                    .findLatestMeasurementDate()
                    .orElseGet(() -> effectiveTo.minusDays(30)));

    return MeasurementSync.of(effectiveFrom, effectiveTo);
  }

  private ResponseEntity<ProblemDetail> buildMeasurementViolationsResponse(
      String detail, List<MeasurementViolation> violations) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);

    problemDetail.setProperty("violations", violations);

    return ResponseEntity.badRequest().body(problemDetail);
  }
}
