package dev.nasenov.miscale2pg.configuration;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectReader;
import tools.jackson.dataformat.csv.CsvMapper;

@Configuration
public class JacksonConfiguration {

  @Bean
  public ObjectReader miScaleMeasurementReader() {
    CsvMapper csvMapper = CsvMapper.builder().build();

    return csvMapper.readerFor(MiScaleMeasurement.class).with(csvMapper.schemaWithHeader());
  }
}
