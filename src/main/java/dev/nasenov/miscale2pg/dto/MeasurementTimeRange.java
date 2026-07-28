package dev.nasenov.miscale2pg.dto;

import jakarta.validation.constraints.AssertTrue;
import java.time.OffsetDateTime;

public record MeasurementTimeRange(OffsetDateTime from, OffsetDateTime to) {

  public static MeasurementTimeRange of(OffsetDateTime from, OffsetDateTime to) {
    return new MeasurementTimeRange(from, to);
  }

  @AssertTrue(message = "'from' must be before 'to'")
  public boolean isTimeRange() {
    return from.isBefore(to);
  }
}
