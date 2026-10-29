package dev.nasenov.miscale2pg.exception;

import dev.nasenov.miscale2pg.dto.MeasurementViolation;
import java.util.List;

public class ZeppLifeExportInvalidException extends ZeppLifeException {

  public ZeppLifeExportInvalidException(List<MeasurementViolation> violations) {
    super("CSV file(s) could not be validated: " + violations);
  }
}
