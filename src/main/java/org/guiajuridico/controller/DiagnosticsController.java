package org.guiajuridico.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticsController {

    @GetMapping("/boom")
    public ResponseEntity<Void> boom() {
        throw new RuntimeException("Teste de exceção proposital para o handler global");
    }
}