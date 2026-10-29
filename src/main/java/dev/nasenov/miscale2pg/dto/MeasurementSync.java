package dev.nasenov.miscale2pg.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MeasurementSync(@NotNull LocalDate from, @NotNull LocalDate to) {

  public static MeasurementSync of(LocalDate from, LocalDate to) {
    return new MeasurementSync(from, to);
  }

  @AssertTrue(message = "'from' must be before or equal to 'to'")
  public boolean isDateRange() {
    return from.isBefore(to) || from.isEqual(to);
  }
}
