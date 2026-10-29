package dev.nasenov.miscale2pg.dto;

import jakarta.validation.Valid;
import java.util.List;

public record ZeppLifeMeasurementCsv(List<@Valid ZeppLifeMeasurement> measurements) {

  public static ZeppLifeMeasurementCsv of(List<ZeppLifeMeasurement> measurements) {
    return new ZeppLifeMeasurementCsv(measurements);
  }
}
