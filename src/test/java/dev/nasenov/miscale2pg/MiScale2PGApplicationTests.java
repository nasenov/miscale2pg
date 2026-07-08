package dev.nasenov.miscale2pg;

import dev.nasenov.miscale2pg.dto.UploadResponse;
import dev.nasenov.miscale2pg.model.Measurement;
import dev.nasenov.miscale2pg.repository.MeasurementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MiScale2PGApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:18.4"));

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
				.expectBody(UploadResponse.class)
				.isEqualTo(new UploadResponse(2, 2));

		Measurement completeMeasurement = Measurement.builder()
				.time(OffsetDateTime.parse("2026-06-23T07:35:53Z"))
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
				.time(OffsetDateTime.parse("2026-06-24T04:33:57Z"))
				.weight(68.2)
				.height(180.0)
				.bmi(21.0)
				.build();

		Stream.of(completeMeasurement, partialMeasurement)
			.forEach(measurement -> assertThat(measurementRepository.findById(measurement.time()))
					.isPresent()
					.get()
					.usingRecursiveComparison()
					.isEqualTo(measurement));
	}

	@Test
	void shouldSaveMeasurementsWhenCsvWithDuplicatesIsUploaded() {
		String csv = """
				time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat
				2026-06-24 04:33:57+0000,68.2,180.0,21.0,null,null,null,null,null,null
				2026-06-24 04:33:57+0000,67.8,180.0,20.9,14.422834,58.705936,2.9538348,1516.0,55.067486,6.0
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
				.expectBody(UploadResponse.class)
				.isEqualTo(new UploadResponse(2, 2));

		Measurement measurement = Measurement.builder()
				.time(OffsetDateTime.parse("2026-06-24T04:33:57Z"))
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

		 assertThat(measurementRepository.findById(measurement.time()))
				 .isPresent()
				 .get()
				 .usingRecursiveComparison()
				 .isEqualTo(measurement);
	}

	@Test
	void shouldReturnContentTooLargeWhenCsvFileGreaterThanMaxFileSizeIsUploaded() {
		byte[] csv = new byte[2 * 1024 * 1024]; // 2 MiB

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new ByteArrayResource(csv) {
			@Override
			public String getFilename() {
				return "measurements.csv";
			}
		});

		restTestClient.post()
				.uri("/api/measurements")
				.body(body)
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.CONTENT_TOO_LARGE)
				.expectBody(ProblemDetail.class)
				.value(response -> {
					assertThat(response).isNotNull();
					assertThat(response.getStatus()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE.value());
					assertThat(response.getDetail()).isEqualTo("Maximum upload size exceeded");
				});
	}

}
