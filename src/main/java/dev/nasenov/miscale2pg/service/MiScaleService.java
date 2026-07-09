package dev.nasenov.miscale2pg.service;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.model.Measurement;
import dev.nasenov.miscale2pg.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MiScaleService {

    private final MeasurementRepository measurementRepository;

    @Transactional
    public int save(Collection<MiScaleMeasurement> miScaleMeasurements) {
        List<Measurement> measurements = miScaleMeasurements
                .stream()
                .map(MiScaleService::convert)
                .toList();

        measurementRepository.saveAll(measurements);

        return measurements.size();
    }

    private static Measurement convert(MiScaleMeasurement miScaleMeasurement) {
        return Measurement.builder()
                .time(miScaleMeasurement.time())
                .weight(miScaleMeasurement.weight())
                .height(miScaleMeasurement.height())
                .bmi(miScaleMeasurement.bmi())
                .fatRate(miScaleMeasurement.fatRate())
                .bodyWaterRate(miScaleMeasurement.bodyWaterRate())
                .boneMass(miScaleMeasurement.boneMass())
                .metabolism(miScaleMeasurement.metabolism())
                .muscleRate(miScaleMeasurement.muscleRate())
                .visceralFat(miScaleMeasurement.visceralFat())
                .build();
    }

}
