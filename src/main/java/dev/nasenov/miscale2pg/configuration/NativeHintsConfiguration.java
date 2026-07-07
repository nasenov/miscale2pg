package dev.nasenov.miscale2pg.configuration;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.model.Measurement;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@RegisterReflectionForBinding({MiScaleMeasurement.class, Measurement.class})
public class NativeHintsConfiguration {
}
