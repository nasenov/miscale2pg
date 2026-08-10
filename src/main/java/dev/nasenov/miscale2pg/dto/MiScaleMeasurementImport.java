package dev.nasenov.miscale2pg.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record MiScaleMeasurementImport(@NotEmpty List<@Valid MiScaleMeasurementCsv> csvs) {

  public static MiScaleMeasurementImport of(List<MiScaleMeasurementCsv> csvs) {
    return new MiScaleMeasurementImport(csvs);
  }
}
