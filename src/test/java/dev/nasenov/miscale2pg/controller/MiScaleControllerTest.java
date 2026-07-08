package dev.nasenov.miscale2pg.controller;

import dev.nasenov.miscale2pg.configuration.JacksonConfiguration;
import dev.nasenov.miscale2pg.dto.UploadResponse;
import dev.nasenov.miscale2pg.service.MiScaleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.Collection;

@WebMvcTest
@Import(JacksonConfiguration.class)
class MiScaleControllerTest {

    @TestBean
    MiScaleService miScaleService;

    static MiScaleService miScaleService() {
        return Collection::size;
    }

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

    @Test
    void shouldReturnBadRequestWhenNonCsvFileIsUploaded() {
        String text = """
                Lorem ipsum dolor sit amet consectetur adipiscing elit.
                Dolor sit amet consectetur adipiscing elit quisque faucibus.
                """;

        mockMvcTester.post()
                .uri("/api/measurements")
                .multipart()
                .file("file", text.getBytes())
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
                .containsExactly(HttpStatus.BAD_REQUEST.value(), "CSV file could not be parsed.");
    }

    @Test
    void shouldReturnBadRequestWhenWrongCsvFileIsUploaded() {
        String csv = """
                id,name,age
                1,John Doe,28
                """;

        mockMvcTester.post()
                .uri("/api/measurements")
                .multipart()
                .file("file", csv.getBytes())
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
                .containsExactly(HttpStatus.BAD_REQUEST.value(), "CSV file could not be parsed.");
    }

    @Test
    void shouldReturnOKWhenHeadersOnlyCsvFileIsUploaded() {
        String csv = "time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat";

        mockMvcTester.post()
                .uri("/api/measurements")
                .multipart()
                .file("file", csv.getBytes())
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(UploadResponse.class)
                .extracting(UploadResponse::total, UploadResponse::saved)
                .containsExactly(0, 0);
    }

}