package dev.nasenov.miscale2pg;

import dev.nasenov.miscale2pg.entity.Measurement;
import dev.nasenov.miscale2pg.repository.MeasurementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MiScale2PGApplicationTests {

	@TestConfiguration(proxyBeanMethods = false)
	static class TestcontainersConfiguration {

		@Bean
		@ServiceConnection
		public PostgreSQLContainer postgreSQLContainer() {
			return new PostgreSQLContainer(DockerImageName.parse("postgres:18.4"));
		}

	}

	@Autowired
	RestTestClient restTestClient;

	@Autowired
	MeasurementRepository measurementRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldSaveCompleteAndPartialMeasurementsWhenCsvIsUploaded() {
		String csv = """
				time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
				2026-06-23 07:35:53+0000,67.8,180.0,20.9,14.422834,58.705936,2.9538348,1516.0,55.067486,6.0
				2026-06-24 04:33:57+0000,68.2,180.0,21.0,null,null,null,null,null,null
				""";

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new ByteArrayResource(csv.getBytes()) {
			@Override
			public String getFilename() {
				return "measurements.csv";
			}
		});

		restTestClient.post()
				.uri("/api/measurements")
				.body(body)
				.exchange()
				.expectStatus().isCreated()
				.expectBody().isEmpty();

		Measurement completeMeasurement = Measurement.builder()
				.time(Instant.parse("2026-06-23T07:35:53Z"))
				.weight(67.8)
				.height(180.0)
				.bmi(20.9)
				.fatRate(14.42)
				.bodyWaterRate(58.71)
				.boneMass(2.95)
				.metabolism(1516.0)
				.muscleRate(55.07)
				.visceralFat(6.0)
				.build();

		Measurement partialMeasurement = Measurement.builder()
				.time(Instant.parse("2026-06-24T04:33:57Z"))
				.weight(68.2)
				.height(180.0)
				.bmi(21.0)
				.build();

		Stream.of(completeMeasurement, partialMeasurement)
			.forEach(measurement -> assertThat(measurementRepository.findById(measurement.getTime()))
					.isPresent()
					.get()
					.usingRecursiveComparison()
					.isEqualTo(measurement));
	}

}
