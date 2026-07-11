package dev.nasenov.miscale2pg.dto;

import jakarta.validation.ConstraintViolation;

public record MiScaleMeasurementViolation(String field, String message) {

  public static MiScaleMeasurementViolation from(
      ConstraintViolation<MiScaleMeasurementImport> violation) {
    return new MiScaleMeasurementViolation(
        violation.getPropertyPath().toString(), violation.getMessage());
  }
}
