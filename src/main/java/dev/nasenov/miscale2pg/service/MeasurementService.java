package dev.nasenov.miscale2pg.service;

import dev.nasenov.miscale2pg.dto.MeasurementResponse;
import dev.nasenov.miscale2pg.dto.MeasurementTimeRange;
import dev.nasenov.miscale2pg.dto.ZeppLifeMeasurement;
import dev.nasenov.miscale2pg.dto.ZeppLifeMeasurementExport;
import dev.nasenov.miscale2pg.model.Measurement;
import dev.nasenov.miscale2pg.repository.MeasurementRepository;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
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

  public Optional<LocalDate> findLatestMeasurementDate() {
    return measurementRepository
        .findLatest()
        .map(Measurement::time)
        .map(time -> time.withOffsetSameInstant(ZoneOffset.UTC).toLocalDate());
  }

  @Transactional
  public void save(ZeppLifeMeasurementExport zeppLifeMeasurementExport) {
    zeppLifeMeasurementExport
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

  private static Measurement convert(ZeppLifeMeasurement zeppLifeMeasurement) {
    return Measurement.builder()
        .time(zeppLifeMeasurement.time())
        .weight(zeppLifeMeasurement.weight())
        .height(zeppLifeMeasurement.height())
        .bmi(zeppLifeMeasurement.bmi())
        .fatRate(zeppLifeMeasurement.fatRate())
        .bodyWaterRate(zeppLifeMeasurement.bodyWaterRate())
        .boneMass(zeppLifeMeasurement.boneMass())
        .metabolism(zeppLifeMeasurement.metabolism())
        .muscleRate(zeppLifeMeasurement.muscleRate())
        .visceralFat(zeppLifeMeasurement.visceralFat())
        .build();
  }
}
