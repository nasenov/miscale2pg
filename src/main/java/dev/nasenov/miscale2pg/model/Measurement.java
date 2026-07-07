package dev.nasenov.miscale2pg.model;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record Measurement(
        OffsetDateTime time,
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
