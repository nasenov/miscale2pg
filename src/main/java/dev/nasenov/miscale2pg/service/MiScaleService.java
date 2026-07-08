package dev.nasenov.miscale2pg.service;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;

import java.util.Collection;

public interface MiScaleService {

    int save(Collection<MiScaleMeasurement> miScaleMeasurements);

}
