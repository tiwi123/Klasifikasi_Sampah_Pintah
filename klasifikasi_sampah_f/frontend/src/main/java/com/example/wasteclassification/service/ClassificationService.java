package com.example.wasteclassification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class ClassificationService {

    @Autowired
    private RestTemplate restTemplate;

    public String classifyImage(MultipartFile image) throws IOException {
        // Simpan gambar ke direktori sementara
        File tempFile = File.createTempFile("upload-", ".jpg");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(image.getBytes());
        }

        // Kirim gambar ke backend Python
        String url = "http://192.168.100.23:5000/classify";  // Pastikan URL ini benar dan server Python berjalan
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Menggunakan InputStreamResource untuk mengirimkan file sebagai bagian dari multipart request
        Resource resource = new InputStreamResource(image.getInputStream());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", resource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Mengirim request ke backend Python
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        // Menghapus file sementara setelah pengiriman
        tempFile.delete();

        // Menangani hasil response dan mengembalikannya
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();  // Kembalikan respons dari backend Python
        } else {
            return "Error: " + response.getStatusCode();
        }
    }
}
