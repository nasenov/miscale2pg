package dev.nasenov.miscale2pg.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Measurement implements Persistable<Instant> {

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

    @Override
    public Instant getId() {
        return time;
    }

    @Override
    public boolean isNew() {
        return true;
    }

}
