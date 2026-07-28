package dev.nasenov.miscale2pg.configuration;

import dev.nasenov.miscale2pg.dto.MeasurementResponse;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.model.Measurement;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@RegisterReflectionForBinding({
  MiScaleMeasurement.class,
  Measurement.class,
  MeasurementResponse.class
})
public class NativeHintsConfiguration {}
