package dev.nasenov.miscale2pg.dto;

import jakarta.validation.ConstraintViolation;

public record MeasurementViolation(String field, String message) {

  public static MeasurementViolation from(ConstraintViolation<?> violation) {
    return new MeasurementViolation(violation.getPropertyPath().toString(), violation.getMessage());
  }
}
