package dev.nasenov.miscale2pg.service;

import dev.nasenov.miscale2pg.dto.MeasurementResponse;
import dev.nasenov.miscale2pg.dto.MeasurementTimeRange;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurementImport;
import dev.nasenov.miscale2pg.model.Measurement;
import dev.nasenov.miscale2pg.repository.MeasurementRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeasurementService {

  private final MeasurementRepository measurementRepository;

  public List<MeasurementResponse> findByTimeRange(MeasurementTimeRange timeRange) {
    return measurementRepository.findByTimeRange(timeRange.from(), timeRange.to()).stream()
        .map(MeasurementService::convert)
        .toList();
  }

  @Transactional
  public void save(MiScaleMeasurementImport miScaleMeasurementImport) {
    miScaleMeasurementImport
        .csvs()
        .forEach(
            csv ->
                measurementRepository.saveAll(
                    csv.measurements().stream().map(MeasurementService::convert).toList()));
  }

  private static MeasurementResponse convert(Measurement measurement) {
    return MeasurementResponse.builder()
        .time(measurement.time())
        .weight(measurement.weight())
        .height(measurement.height())
        .bmi(measurement.bmi())
        .fatRate(measurement.fatRate())
        .bodyWaterRate(measurement.bodyWaterRate())
        .boneMass(measurement.boneMass())
        .metabolism(measurement.metabolism())
        .muscleRate(measurement.muscleRate())
        .visceralFat(measurement.visceralFat())
        .build();
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
