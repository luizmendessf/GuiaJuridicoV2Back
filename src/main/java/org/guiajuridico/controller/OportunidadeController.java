package org.guiajuridico.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/oportunidades")
public class OportunidadeController {

    @GetMapping("/todas")
    public ResponseEntity<List<Object>> getTodas() {
        // Placeholder until service/repository is wired
        return ResponseEntity.ok(Collections.emptyList());
    }
}