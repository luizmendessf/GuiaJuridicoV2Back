package org.guiajuridico.controller;

import org.guiajuridico.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String filename = imageService.saveImage(file);
            String url = "/api/images/" + filename;
            return ResponseEntity.ok().body(new ImageUploadResponse(filename, url));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Falha ao salvar imagem: " + e.getMessage());
        }
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Resource resource = imageService.loadImage(filename);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename)
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<Void> deleteImage(@PathVariable String filename) {
        try {
            boolean deleted = imageService.deleteImage(filename);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    static class ImageUploadResponse {
        public String filename;
        public String url;
        public ImageUploadResponse(String filename, String url) {
            this.filename = filename;
            this.url = url;
        }
    }
}