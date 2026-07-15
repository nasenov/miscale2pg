package dev.nasenov.miscale2pg.dto;

import jakarta.validation.Valid;
import java.util.List;

public record MiScaleMeasurementImport(List<@Valid MiScaleMeasurement> measurements) {

  public static MiScaleMeasurementImport of(List<MiScaleMeasurement> measurements) {
    return new MiScaleMeasurementImport(measurements);
  }
}
