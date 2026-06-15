package dev.nasenov.miscale2pg.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/measurements")
public class MiScaleController {

    @PostMapping
    public ResponseEntity<Void> upload(@RequestParam MultipartFile file) {
        log.info("File {} size: {}", file.getOriginalFilename(), file.getSize());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
