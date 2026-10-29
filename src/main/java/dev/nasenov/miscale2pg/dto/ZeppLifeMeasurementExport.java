package dev.nasenov.miscale2pg.dto;

import jakarta.validation.Valid;
import java.util.List;

public record ZeppLifeMeasurementExport(List<@Valid ZeppLifeMeasurementCsv> csvs) {

  public static ZeppLifeMeasurementExport of(List<ZeppLifeMeasurementCsv> csvs) {
    return new ZeppLifeMeasurementExport(csvs);
  }
}
