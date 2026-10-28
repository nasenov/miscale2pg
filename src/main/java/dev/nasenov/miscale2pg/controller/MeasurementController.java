package dev.nasenov.miscale2pg.controller;

import dev.nasenov.miscale2pg.dto.MeasurementSync;
import dev.nasenov.miscale2pg.dto.MeasurementTimeRange;
import dev.nasenov.miscale2pg.dto.MeasurementViolation;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurementImport;
import dev.nasenov.miscale2pg.exception.MiScaleUnauthorizedException;
import dev.nasenov.miscale2pg.service.MeasurementService;
import dev.nasenov.miscale2pg.service.MiScaleClientService;
import jakarta.validation.Validator;
import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.dataformat.csv.CsvReadException;

@Slf4j
@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementController {

  private final Validator validator;

  private final MeasurementService measurementService;

  private final MiScaleClientService miScaleClientService;

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
      @RequestHeader("App-Token") String appToken,
      @RequestParam String userId,
      @RequestParam String email,
      @RequestParam Optional<LocalDate> startDate,
      @RequestParam Optional<LocalDate> endDate)
      throws IOException {
    MeasurementSync measurementSync =
        buildMeasurementSync(appToken, userId, email, startDate, endDate);

    List<MeasurementViolation> violations =
        validator.validate(measurementSync).stream().map(MeasurementViolation::from).toList();

    if (!violations.isEmpty()) {
      return buildMeasurementViolationsResponse("Invalid measurement sync request.", violations);
    }

    MiScaleMeasurementImport measurementsImport = miScaleClientService.sync(measurementSync);

    List<MeasurementViolation> importViolations =
        validator.validate(measurementsImport).stream().map(MeasurementViolation::from).toList();

    if (!importViolations.isEmpty()) {
      log.error("CSV file(s) could not be validated: {}", importViolations);
      return ResponseEntity.internalServerError()
          .body(
              ProblemDetail.forStatusAndDetail(
                  HttpStatus.INTERNAL_SERVER_ERROR,
                  "An unexpected error occurred. Please try again later."));
    }

    measurementService.save(measurementsImport);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @ExceptionHandler(MiScaleUnauthorizedException.class)
  public ProblemDetail handleMiScaleUnauthorizedException(MiScaleUnauthorizedException ex) {
    log.error("Failed tp authenticate", ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Unable to authenticate with the provided App-Token header value.");
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
        HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
  }

  @ExceptionHandler({CsvReadException.class, MismatchedInputException.class, IOException.class})
  public ProblemDetail handleFileException(Exception ex) {
    log.error("Failed to read file", ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
  }

  @ExceptionHandler(DataAccessException.class)
  public ProblemDetail handleDataAccessException(DataAccessException ex) {
    log.error("Database exception", ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
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

  private MeasurementSync buildMeasurementSync(
      String appToken,
      String userId,
      String email,
      Optional<LocalDate> startDate,
      Optional<LocalDate> endDate) {
    LocalDate effectiveEndDate = endDate.orElseGet(LocalDate::now);
    LocalDate effectiveStartDate =
        startDate.orElseGet(
            () ->
                measurementService
                    .findLatestMeasurementDate()
                    .orElseGet(() -> effectiveEndDate.minusDays(30)));

    return MeasurementSync.of(appToken, userId, email, effectiveStartDate, effectiveEndDate);
  }

  private ResponseEntity<ProblemDetail> buildMeasurementViolationsResponse(
      String detail, List<MeasurementViolation> violations) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);

    problemDetail.setProperty("violations", violations);

    return ResponseEntity.badRequest().body(problemDetail);
  }
}
