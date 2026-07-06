package dev.nasenov.miscale2pg.controller;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.dto.UploadResponse;
import dev.nasenov.miscale2pg.service.MiScaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<UploadResponse> upload(@RequestParam MultipartFile file) throws IOException {
        try (MappingIterator<MiScaleMeasurement> iterator = miScaleMeasurementReader.readValues(file.getBytes())) {
            List<MiScaleMeasurement> measurements = iterator.readAll();

            int saved = miScaleService.save(measurements);

            return ResponseEntity.status(saved != 0 ? HttpStatus.CREATED : HttpStatus.OK)
                    .body(new UploadResponse(measurements.size(), saved));
        }
    }

    @ExceptionHandler(CsvReadException.class)
    public ResponseEntity<ProblemDetail> handleCsvReadException(CsvReadException ex) {
        log.error("Failed to read CSV file", ex);
        return ResponseEntity.badRequest()
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "CSV file could not be read."));
    }

    @ExceptionHandler(MismatchedInputException.class)
    public ResponseEntity<ProblemDetail> handleMismatchedInputException(MismatchedInputException ex) {
        log.error("Failed to parse CSV file", ex);
        return ResponseEntity.badRequest()
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "CSV file could not be parsed."));
    }

}
