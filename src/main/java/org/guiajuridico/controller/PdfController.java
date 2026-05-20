package org.guiajuridico.controller;

import org.guiajuridico.service.PdfCoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

@RestController
@RequestMapping("/api/pdfs")
public class PdfController {

    private final Path uploadDir;

    @Autowired
    private PdfCoverService pdfCoverService;

    public PdfController(@Value("${pdf.upload.dir}") String uploadDirString) {
        this.uploadDir = Paths.get(uploadDirString);
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getPdf(@PathVariable String fileName) {
        try {
            if (!isSafePdfFileName(fileName)) {
                return ResponseEntity.badRequest().build();
            }
            Path filePath = uploadDir.resolve(fileName).normalize();
            if (!filePath.startsWith(uploadDir.normalize())) {
                return ResponseEntity.badRequest().build();
            }
            Resource resource = new FileSystemResource(filePath.toFile());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName.replace("\"", "") + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{fileName}/cover")
    public ResponseEntity<Resource> getPdfCover(@PathVariable String fileName) {
        try {
            if (!isSafePdfFileName(fileName)) {
                return ResponseEntity.badRequest().build();
            }
            Path coverPath = pdfCoverService.resolveCoverPath(fileName);
            Resource resource = new FileSystemResource(coverPath.toFile());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo PDF ausente");
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        if (!original.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body("Apenas arquivos .pdf são aceitos");
        }
        try {
            Files.createDirectories(uploadDir);
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
                    .path("/api/pdfs/")
                    .path(filename)
                    .toUriString();

            return ResponseEntity.ok().body(new PdfUploadResponse(url, filename));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Falha ao salvar o PDF");
        }
    }

    private static boolean isSafePdfFileName(String name) {
        if (name == null || name.isBlank()) return false;
        if (name.contains("..") || name.contains("/") || name.contains("\\")) return false;
        return name.toLowerCase().endsWith(".pdf");
    }

    public static class PdfUploadResponse {
        public String url;
        public String filename;

        public PdfUploadResponse(String url, String filename) {
            this.url = url;
            this.filename = filename;
        }
    }
}
