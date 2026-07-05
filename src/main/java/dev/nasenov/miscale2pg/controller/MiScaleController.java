package dev.nasenov.miscale2pg.controller;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
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
import tools.jackson.dataformat.csv.CsvReadException;

import java.io.IOException;

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
            miScaleService.save(iterator.readAll());

            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }

    @ExceptionHandler(CsvReadException.class)
    public ResponseEntity<ProblemDetail> handleCsvReadException(CsvReadException ex) {
        log.error("Failed to read CSV file", ex);
        return ResponseEntity.badRequest()
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "CSV file could not be read."));
    }


}
