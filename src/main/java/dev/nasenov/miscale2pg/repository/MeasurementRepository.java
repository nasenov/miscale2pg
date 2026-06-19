package dev.nasenov.miscale2pg.repository;

import dev.nasenov.miscale2pg.entity.Measurement;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface MeasurementRepository extends CrudRepository<Measurement, Instant> {

}
