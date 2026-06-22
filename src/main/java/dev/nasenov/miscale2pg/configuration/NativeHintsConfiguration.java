package dev.nasenov.miscale2pg.configuration;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@RegisterReflectionForBinding(MiScaleMeasurement.class)
public class NativeHintsConfiguration {
}
