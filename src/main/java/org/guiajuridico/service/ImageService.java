package org.guiajuridico.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${upload.dir:uploads/images}")
    private String uploadDir;

    public String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Arquivo vazio");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        String newFilename = UUID.randomUUID() + ext.toLowerCase();
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        Path target = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return newFilename;
    }

    public Resource loadImage(String filename) throws MalformedURLException {
        Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        }
        return null;
    }

    public boolean deleteImage(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename);
        if (Files.exists(filePath)) {
            return Files.deleteIfExists(filePath);
        }
        return false;
    }
}