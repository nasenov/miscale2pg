package dev.nasenov.miscale2pg.configuration;

import dev.nasenov.miscale2pg.dto.MeasurementResponse;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.model.Measurement;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(NativeHintsConfiguration.HibernateValidatorRuntimeHints.class)
@RegisterReflectionForBinding({
  MiScaleMeasurement.class,
  Measurement.class,
  MeasurementResponse.class
})
public class NativeHintsConfiguration {

  static class HibernateValidatorRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
      hints
          .reflection()
          .registerType(
              TypeReference.of("org.hibernate.validator.internal.util.logging.Log_$logger"),
              builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
      hints
          .reflection()
          .registerType(
              TypeReference.of("org.hibernate.validator.internal.util.logging.Messages_$bundle"),
              builder ->
                  builder
                      .withMembers(MemberCategory.INVOKE_DECLARED_METHODS)
                      .withField("INSTANCE"));
    }
  }
}
