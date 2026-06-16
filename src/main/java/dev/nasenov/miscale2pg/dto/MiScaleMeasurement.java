package dev.nasenov.miscale2pg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record MiScaleMeasurement(
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ssXX")
        Instant time,
        double weight,
        double height,
        double bmi,
        Double fatRate,
        Double bodyWaterRate,
        Double boneMass,
        Double metabolism,
        Double muscleRate,
        Double visceralFat
) {
}
