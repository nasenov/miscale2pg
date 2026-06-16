package dev.nasenov.miscale2pg.controller;

import dev.nasenov.miscale2pg.service.MiScaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MiScaleController {

    private final MiScaleService miScaleService;

    @PostMapping
    public ResponseEntity<Void> upload(@RequestParam MultipartFile file) {
        miScaleService.parseMiScaleCSV(file);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
