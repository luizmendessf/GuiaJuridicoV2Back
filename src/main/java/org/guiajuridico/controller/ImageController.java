package org.guiajuridico.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @GetMapping("/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        try {
            Resource resource = new ClassPathResource("static/images/" + imageName);
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // Determina o tipo de mídia baseado na extensão do arquivo
            MediaType mediaType = MediaType.IMAGE_JPEG; // padrão
            if (imageName.toLowerCase().endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (imageName.toLowerCase().endsWith(".gif")) {
                mediaType = MediaType.IMAGE_GIF;
            }
            
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}