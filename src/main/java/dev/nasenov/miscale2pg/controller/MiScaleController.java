package dev.nasenov.miscale2pg.controller;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.service.MiScaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectReader;

import java.io.IOException;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MiScaleController {

    private final ObjectReader miScaleMeasurementReader;

    private final MiScaleService miScaleService;

    @PostMapping
    public ResponseEntity<Void> upload(@RequestParam MultipartFile file) {
        try {
            Collection<MiScaleMeasurement> miScaleMeasurements = miScaleMeasurementReader.<MiScaleMeasurement>readValues(file.getInputStream()).readAll();

            miScaleService.save(miScaleMeasurements);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IOException e) {
            log.error("Could not parse CSV file: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
