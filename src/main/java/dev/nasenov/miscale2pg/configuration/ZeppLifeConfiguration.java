package dev.nasenov.miscale2pg.configuration;

import dev.nasenov.miscale2pg.dto.ZeppLifeMeasurement;
import dev.nasenov.miscale2pg.exception.ZeppLifeExportNotReadyException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectReader;
import tools.jackson.dataformat.csv.CsvMapper;

@Configuration
@EnableConfigurationProperties(ZeppLifeConfiguration.ZeppLifeProperties.class)
public class ZeppLifeConfiguration {

  @Validated
  @ConfigurationProperties("app.zepp-life")
  public record ZeppLifeProperties(
      @NotBlank String baseUrl,
      @NotBlank String appToken,
      @NotBlank String userId,
      @NotBlank @Email String email) {}

  @Bean
  public RestClient zeppLifeRestClient(
      RestClient.Builder builder, ZeppLifeProperties zeppLifeProperties) {
    return builder.baseUrl(zeppLifeProperties.baseUrl()).build();
  }

  @Bean
  public RestClient s3RestClient(RestClient.Builder builder) {
    return builder.build();
  }

  @Bean
  public RetryTemplate zeppLifeRetryTemplate() {
    RetryPolicy retryPolicy =
        RetryPolicy.builder()
            .maxRetries(3)
            .delay(Duration.ofSeconds(1))
            .multiplier(5.0)
            .includes(ZeppLifeExportNotReadyException.class)
            .build();

    return new RetryTemplate(retryPolicy);
  }

  @Bean
  public ObjectReader zeppLifeMeasurementReader() {
    CsvMapper csvMapper = CsvMapper.builder().build();

    return csvMapper.readerFor(ZeppLifeMeasurement.class).with(csvMapper.schemaWithHeader());
  }
}
