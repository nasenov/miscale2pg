package dev.nasenov.miscale2pg.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record MiScaleMeasurementImport(@NotEmpty List<@Valid MiScaleMeasurement> measurements) {

  public static MiScaleMeasurementImport of(List<MiScaleMeasurement> measurements) {
    return new MiScaleMeasurementImport(measurements);
  }
}
