package dev.nasenov.miscale2pg.repository;

import dev.nasenov.miscale2pg.model.Measurement;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MeasurementRepository {

  private static final String FIND_BY_ID_SQL =
      """
      SELECT *
      FROM measurement
      WHERE "time" = :id
      """;

  private static final String UPSERT_SQL =
      """
      INSERT INTO measurement (
          "time",
          weight,
          height,
          bmi,
          fat_rate,
          body_water_rate,
          bone_mass,
          metabolism,
          muscle_rate,
          visceral_fat
      )
      VALUES (
          :time,
          :weight,
          :height,
          :bmi,
          :fatRate,
          :bodyWaterRate,
          :boneMass,
          :metabolism,
          :muscleRate,
          :visceralFat
      )
      ON CONFLICT ("time")
      DO UPDATE SET
          weight = EXCLUDED.weight,
          height = EXCLUDED.height,
          bmi = EXCLUDED.bmi,
          fat_rate = EXCLUDED.fat_rate,
          body_water_rate = EXCLUDED.body_water_rate,
          bone_mass = EXCLUDED.bone_mass,
          metabolism = EXCLUDED.metabolism,
          muscle_rate = EXCLUDED.muscle_rate,
          visceral_fat = EXCLUDED.visceral_fat
      """;

  private final JdbcClient jdbcClient;

  public Optional<Measurement> findById(OffsetDateTime id) {
    return jdbcClient.sql(FIND_BY_ID_SQL).param("id", id).query(Measurement.class).optional();
  }

  public void saveAll(Collection<Measurement> measurements) {
    measurements.forEach(
        measurement -> jdbcClient.sql(UPSERT_SQL).paramSource(measurement).update());
  }
}
