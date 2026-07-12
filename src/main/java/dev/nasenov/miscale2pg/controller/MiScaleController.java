package dev.nasenov.miscale2pg.controller;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurementImport;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurementViolation;
import dev.nasenov.miscale2pg.service.MiScaleService;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
public class MiScaleController {

  private final ObjectReader miScaleMeasurementReader;

  private final Validator validator;

  private final MiScaleService miScaleService;

  @PostMapping
  public ResponseEntity<?> upload(@RequestParam MultipartFile file) {
    try (MappingIterator<MiScaleMeasurement> iterator =
        miScaleMeasurementReader.readValues(file.getBytes())) {

      MiScaleMeasurementImport measurementsImport = MiScaleMeasurementImport.of(iterator.readAll());
      List<MiScaleMeasurement> measurements = measurementsImport.measurements();

      if (measurements.isEmpty()) {
        return ResponseEntity.ok().build();
      }

      List<MiScaleMeasurementViolation> violations =
          validator.validate(measurementsImport).stream()
              .map(MiScaleMeasurementViolation::from)
              .toList();

      if (!violations.isEmpty()) {
        ProblemDetail problemDetail =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "CSV file contains invalid measurement(s).");

        problemDetail.setProperty("violations", violations);

        return ResponseEntity.badRequest().body(problemDetail);
      }

      miScaleService.save(measurements);

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
    log.error("Failed to save measurements", ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
  }
}
