package org.guiajuridico.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/oportunidades")
public class OportunidadesController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/todas")
    public ResponseEntity<JsonNode> listarTodas() throws IOException {
        ClassPathResource resource = new ClassPathResource("oportunidades.json");
        try (InputStream is = resource.getInputStream()) {
            JsonNode json = objectMapper.readTree(is);
            return ResponseEntity.ok(json);
        }
    }
}