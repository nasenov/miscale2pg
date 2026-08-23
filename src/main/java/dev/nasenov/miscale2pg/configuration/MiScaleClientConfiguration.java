package dev.nasenov.miscale2pg.configuration;

import dev.nasenov.miscale2pg.exception.MiScaleExportNotReadyException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.web.client.RestClient;

@Configuration
public class MiScaleClientConfiguration {

  @Bean
  public RestClient miScaleRestClient(
      RestClient.Builder builder, @Value("${app.miscale.api.base-url}") String baseUrl) {
    return builder.baseUrl(baseUrl).build();
  }

  @Bean
  public RestClient s3RestClient(RestClient.Builder builder) {
    return builder.build();
  }

  @Bean
  public RetryTemplate miScaleRetryTemplate() {
    RetryPolicy retryPolicy =
        RetryPolicy.builder()
            .maxRetries(3)
            .delay(Duration.ofSeconds(1))
            .multiplier(5.0)
            .includes(MiScaleExportNotReadyException.class)
            .build();

    return new RetryTemplate(retryPolicy);
  }
}
