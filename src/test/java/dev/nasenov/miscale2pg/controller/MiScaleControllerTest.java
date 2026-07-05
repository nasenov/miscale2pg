package dev.nasenov.miscale2pg.controller;

import dev.nasenov.miscale2pg.configuration.JacksonConfiguration;
import dev.nasenov.miscale2pg.service.MiScaleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest
@Import(value = JacksonConfiguration.class)
class MiScaleControllerTest {

    @MockitoBean
    MiScaleService miScaleService;

    @Autowired
    MockMvcTester mockMvcTester;

    @Test
    void shouldReturnBadRequestWhenNoFileIsUploaded() {
        mockMvcTester.post()
            .uri("/api/measurements")
            .multipart()
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .convertTo(ProblemDetail.class)
            .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
            .containsExactly(HttpStatus.BAD_REQUEST.value(), "Required part 'file' is not present.");
    }

    @Test
    void shouldReturnBadRequestWhenEmptyFileIsUploaded() {
        mockMvcTester.post()
                .uri("/api/measurements")
                .multipart()
                .file("file", "".getBytes())
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
                .containsExactly(HttpStatus.BAD_REQUEST.value(), "CSV file could not be read.");
    }

}