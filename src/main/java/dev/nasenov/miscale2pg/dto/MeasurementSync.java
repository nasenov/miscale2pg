package dev.nasenov.miscale2pg.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MeasurementSync(
    @NotBlank String appToken,
    @NotBlank String userId,
    @NotNull @Email String email,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate) {

  public static MeasurementSync of(
      String appToken, String userId, String email, LocalDate startDate, LocalDate endDate) {
    return new MeasurementSync(appToken, userId, email, startDate, endDate);
  }

  @AssertTrue(message = "'startDate' must be before or equal to 'endDate'")
  public boolean isDateRange() {
    return startDate.isBefore(endDate) || startDate.isEqual(endDate);
  }
}
