package dev.nasenov.miscale2pg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record MiScaleMeasurement(
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ssXX") OffsetDateTime time,
    @DecimalMin(value = "5.0") @DecimalMax(value = "150.0") double weight,
    @DecimalMin(value = "90.0") @DecimalMax(value = "220.0") double height,
    double bmi,
    @DecimalMin(value = "5.0") @DecimalMax(value = "50.0") Double fatRate,
    @DecimalMin(value = "40.0") @DecimalMax(value = "80.0") Double bodyWaterRate,
    @DecimalMin(value = "0.0", inclusive = false) @DecimalMax(value = "20.0", inclusive = false) Double boneMass,
    @DecimalMin(value = "1000.0") @DecimalMax(value = "3000.0") Double metabolism,
    @DecimalMin(value = "0.0", inclusive = false) Double muscleRate,
    @DecimalMin(value = "0.0") @DecimalMax(value = "30.0") Double visceralFat) {

  @AssertTrue(message = "must be equal to weight / height² (height in meters)")
  public boolean isBmi() {
    double calculated = weight / Math.pow(height / 100, 2);

    return Math.abs(bmi - calculated) <= 0.1;
  }

  @AssertTrue(message = "must be less than weight") public boolean isBoneMass() {
    return boneMass == null || boneMass < weight;
  }

  @AssertTrue(message = "must be less than weight") public boolean isMuscleRate() {
    return muscleRate == null || muscleRate < weight;
  }

  @AssertTrue(message = "must be equal to weight within 1 unit") public boolean isBodyComposition() {
    if (fatRate == null || boneMass == null || muscleRate == null) {
      return true;
    }

    double calculated = (fatRate / 100 * weight) + boneMass + muscleRate;

    return Math.abs(weight - calculated) <= 1;
  }
}
