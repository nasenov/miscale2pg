package dev.nasenov.miscale2pg.service;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectReader;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiScaleService {

    private final ObjectReader miScaleMeasurementsReader;

    public void parseMiScaleCSV(MultipartFile file) {
        try {
            MappingIterator<MiScaleMeasurement> iterator = miScaleMeasurementsReader.readValues(file.getInputStream());

            iterator.readAll()
                    .forEach(measurement -> log.info("Scale Measurement {}, weight = {}", measurement.time(), measurement.weight()));
        } catch (IOException e) {
            log.error("Could not parse CSV file: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
