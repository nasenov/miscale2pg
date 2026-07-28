package dev.nasenov.miscale2pg.controller;

import dev.nasenov.miscale2pg.dto.MeasurementTimeRange;
import dev.nasenov.miscale2pg.dto.MeasurementViolation;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurementImport;
import dev.nasenov.miscale2pg.service.MeasurementService;
import jakarta.validation.Validator;
import java.io.IOException;
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
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.dataformat.csv.CsvReadException;

@Slf4j
@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementController {

  private final ObjectReader miScaleMeasurementReader;

  private final Validator validator;

  private final MeasurementService measurementService;

  @GetMapping
  public ResponseEntity<?> findByTimeRange(
      @RequestParam Optional<OffsetDateTime> from, @RequestParam Optional<OffsetDateTime> to) {
    MeasurementTimeRange timeRange = buildMeasurementTimeRange(from, to);

    List<MeasurementViolation> violations =
        validator.validate(timeRange).stream().map(MeasurementViolation::from).toList();

    if (!violations.isEmpty()) {
      return buildMeasurementViolationsResponse("Invalid measurements filter.", violations);
    }

    return ResponseEntity.ok(measurementService.findByTimeRange(timeRange));
  }

  @PostMapping
  public ResponseEntity<?> upload(@RequestParam MultipartFile file) {
    try (MappingIterator<MiScaleMeasurement> iterator =
        miScaleMeasurementReader.readValues(file.getBytes())) {

      MiScaleMeasurementImport measurementsImport = MiScaleMeasurementImport.of(iterator.readAll());

      if (measurementsImport.measurements().isEmpty()) {
        return ResponseEntity.ok().build();
      }

      List<MeasurementViolation> violations =
          validator.validate(measurementsImport).stream().map(MeasurementViolation::from).toList();

      if (!violations.isEmpty()) {
        return buildMeasurementViolationsResponse(
            "CSV file contains invalid measurement(s).", violations);
      }

      measurementService.save(measurementsImport);

      return ResponseEntity.status(HttpStatus.CREATED).build();
    } catch (IOException ex) {
      log.error("Failed to read file", ex);
      return ResponseEntity.internalServerError()
          .body(
              ProblemDetail.forStatusAndDetail(
                  HttpStatus.INTERNAL_SERVER_ERROR,
                  "An unexpected error occurred. Please try again later."));
    }
  }

  @ExceptionHandler(CsvReadException.class)
  public ProblemDetail handleCsvReadException(CsvReadException ex) {
    log.error("Failed to read CSV file", ex);
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "CSV file could not be read.");
  }

  @ExceptionHandler(MismatchedInputException.class)
  public ProblemDetail handleMismatchedInputException(MismatchedInputException ex) {
    log.error("Failed to parse CSV file", ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "CSV file could not be parsed.");
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

  private ResponseEntity<ProblemDetail> buildMeasurementViolationsResponse(
      String detail, List<MeasurementViolation> violations) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);

    problemDetail.setProperty("violations", violations);

    return ResponseEntity.badRequest().body(problemDetail);
  }
}
