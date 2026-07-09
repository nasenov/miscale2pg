package dev.nasenov.miscale2pg.model;

import java.time.OffsetDateTime;
import lombok.Builder;

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
    Double visceralFat) {}
