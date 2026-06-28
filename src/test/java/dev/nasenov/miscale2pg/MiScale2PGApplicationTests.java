package dev.nasenov.miscale2pg;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
class MiScale2PGApplicationTests {

	@TestConfiguration(proxyBeanMethods = false)
	static class TestcontainersConfiguration {

		@Bean
		@ServiceConnection
		public PostgreSQLContainer postgreSQLContainer() {
			return new PostgreSQLContainer(DockerImageName.parse("postgres:18.4"));
		}

	}

	@Test
	void contextLoads() {
	}

}
