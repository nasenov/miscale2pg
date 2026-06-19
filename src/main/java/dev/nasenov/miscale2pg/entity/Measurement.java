package dev.nasenov.miscale2pg.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {

    @Id
    private Instant time;

    private double weight;

    private double height;

    private double bmi;

    private Double fatRate;

    private Double bodyWaterRate;

    private Double boneMass;

    private Double metabolism;

    private Double muscleRate;

    private Double visceralFat;
}
