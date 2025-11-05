package org.guiajuridico.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final Path uploadDir = Paths.get("uploads/images");

    @GetMapping("/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        try {
            // Primeiro tenta arquivo no filesystem (uploads)
            Path filePath = uploadDir.resolve(imageName).normalize();
            Resource fsResource = new FileSystemResource(filePath.toFile());

            Resource resource;
            if (fsResource.exists() && fsResource.isReadable()) {
                resource = fsResource;
            } else {
                // Fallback para recursos estáticos empacotados
                resource = new ClassPathResource("static/images/" + imageName);
                if (!resource.exists()) {
                    return ResponseEntity.notFound().build();
                }
            }

            MediaType mediaType = MediaType.IMAGE_JPEG; // padrão
            String lower = imageName.toLowerCase();
            if (lower.endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (lower.endsWith(".gif")) {
                mediaType = MediaType.IMAGE_GIF;
            } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            }

            return ResponseEntity.ok().contentType(mediaType).body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo de imagem ausente");
        }
        try {
            Files.createDirectories(uploadDir);
            String original = StringUtils.cleanPath(file.getOriginalFilename());
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                ext = original.substring(dot);
            }
            String base = dot > 0 ? original.substring(0, dot) : original;
            String safeBase = base.replaceAll("[^a-zA-Z0-9-_]", "_");
            String filename = safeBase + "_" + Instant.now().toEpochMilli() + ext;

            Path target = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/api/images/")
                    .path(filename)
                    .toUriString();

            return ResponseEntity.ok().body(new ImageUploadResponse(url, filename));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Falha ao salvar imagem");
        }
    }

    static class ImageUploadResponse {
        public String url;
        public String filename;
        ImageUploadResponse(String url, String filename) {
            this.url = url;
            this.filename = filename;
        }
    }
}