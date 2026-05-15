package org.guiajuridico.controller;

import org.guiajuridico.dto.ApiErrorDto;
import org.guiajuridico.dto.LibraryDocumentDetailDto;
import org.guiajuridico.dto.LibraryDocumentSummaryDto;
import org.guiajuridico.dto.LibraryDocumentUpsertDto;
import org.guiajuridico.service.LibraryDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biblioteca")
public class LibraryDocumentController {

    @Autowired
    private LibraryDocumentService libraryDocumentService;

    @GetMapping
    public ResponseEntity<List<LibraryDocumentSummaryDto>> listarPublicados() {
        return ResponseEntity.ok(libraryDocumentService.listarPublicados());
    }

    @GetMapping("/{idOuSlug}")
    public ResponseEntity<LibraryDocumentDetailDto> buscarPublicadoPorIdOuSlug(@PathVariable String idOuSlug) {
        try {
            return ResponseEntity.ok(libraryDocumentService.buscarPublicadoPorIdOuSlug(idOuSlug));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<List<LibraryDocumentSummaryDto>> listarParaAdmin() {
        return ResponseEntity.ok(libraryDocumentService.listarTodosParaAdmin());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<LibraryDocumentDetailDto> buscarParaAdmin(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(libraryDocumentService.buscarPorIdParaAdmin(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody LibraryDocumentUpsertDto dto) {
        try {
            LibraryDocumentDetailDto created = libraryDocumentService.criar(dto);
            return ResponseEntity.status(201).body(created);
        } catch (RuntimeException e) {
            String msg = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage()
                    : "Não foi possível criar o documento.";
            return ResponseEntity.badRequest().body(new ApiErrorDto(msg));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @RequestBody LibraryDocumentUpsertDto dto) {
        try {
            return ResponseEntity.ok(libraryDocumentService.atualizar(id, dto));
        } catch (RuntimeException e) {
            String msg = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage()
                    : "Não foi possível atualizar o documento.";
            return ResponseEntity.badRequest().body(new ApiErrorDto(msg));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        libraryDocumentService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
