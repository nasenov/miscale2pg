package dev.nasenov.miscale2pg.controller;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.service.MiScaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.dataformat.csv.CsvReadException;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MiScaleController {

    private final ObjectReader miScaleMeasurementReader;

    private final MiScaleService miScaleService;

    @PostMapping
    public ResponseEntity<Void> upload(@RequestParam MultipartFile file) throws IOException {
        try (MappingIterator<MiScaleMeasurement> iterator = miScaleMeasurementReader.readValues(file.getBytes())) {
            List<MiScaleMeasurement> measurements = iterator.readAll();

            if (measurements.isEmpty()) {
                return ResponseEntity.ok().build();
            }

            miScaleService.save(measurements);

            return ResponseEntity.status(HttpStatus.CREATED).build();
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
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "CSV file could not be parsed.");
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDataAccessException(DataAccessException ex) {
        log.error("Failed to save measurements", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
    }

}
