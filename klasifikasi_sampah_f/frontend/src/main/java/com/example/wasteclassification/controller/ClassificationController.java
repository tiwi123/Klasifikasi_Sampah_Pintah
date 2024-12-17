package com.example.wasteclassification.controller;

import com.example.wasteclassification.service.ClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ClassificationController {

    @Autowired
    private ClassificationService classificationService;

    private static final Logger logger = LoggerFactory.getLogger(ClassificationController.class);

    /**
     * Endpoint untuk mengklasifikasikan gambar yang diunggah.
     * 
     * @param image - File gambar yang diunggah oleh klien.
     * @return ResponseEntity dengan hasil klasifikasi atau pesan kesalahan.
     */
    @PostMapping("/classify")
    public ResponseEntity<String> classify(@RequestParam("image") MultipartFile image) {
        // Validasi jika file gambar kosong
        if (image.isEmpty()) {
            logger.warn("Tidak ada file gambar yang diunggah.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tidak ada file gambar yang diunggah.");
        }

        try {
            // Log detail file
            logger.info("Mengklasifikasikan gambar dengan nama file: {}", image.getOriginalFilename());

            // Panggil service untuk mengklasifikasikan gambar
            String result = classificationService.classifyImage(image);

            // Log hasil klasifikasi
            logger.info("Hasil klasifikasi: {}", result);

            // Kembalikan hasil klasifikasi yang berhasil dengan status 200
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            // Log kesalahan dengan jejak tumpukan
            logger.error("Kesalahan saat memproses gambar: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kesalahan saat memproses gambar: " + e.getMessage());
        } catch (Exception e) {
            // Log kesalahan tak terduga dengan jejak tumpukan
            logger.error("Kesalahan tak terduga terjadi: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Terjadi kesalahan tak terduga: " + e.getMessage());
        }
    }
}