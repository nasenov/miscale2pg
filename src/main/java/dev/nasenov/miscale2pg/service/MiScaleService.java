package dev.nasenov.miscale2pg.service;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.entity.Measurement;
import dev.nasenov.miscale2pg.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectReader;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiScaleService {

    private final ObjectReader miScaleMeasurementsReader;

    private final MeasurementRepository measurementRepository;

    public void parseMiScaleCSV(MultipartFile file) {
        try {
            MappingIterator<MiScaleMeasurement> iterator = miScaleMeasurementsReader.readValues(file.getInputStream());

            List<Measurement> measurements = iterator.readAll()
                    .stream()
                    .map(this::convertDtoToEntity)
                    .toList();

            measurementRepository.saveAll(measurements);
        } catch (IOException e) {
            log.error("Could not parse CSV file: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Measurement convertDtoToEntity(MiScaleMeasurement miScaleMeasurement) {
        return new Measurement(
          miScaleMeasurement.time(),
          miScaleMeasurement.weight(),
          miScaleMeasurement.height(),
          miScaleMeasurement.bmi(),
          miScaleMeasurement.fatRate(),
          miScaleMeasurement.bodyWaterRate(),
          miScaleMeasurement.boneMass(),
          miScaleMeasurement.metabolism(),
          miScaleMeasurement.muscleRate(),
          miScaleMeasurement.visceralFat()
        );
    }

}
