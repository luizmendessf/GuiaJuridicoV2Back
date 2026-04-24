package org.guiajuridico.controller;

import org.guiajuridico.dto.BlogArticleDetailDto;
import org.guiajuridico.dto.BlogArticleSummaryDto;
import org.guiajuridico.dto.BlogArticleUpsertDto;
import org.guiajuridico.service.BlogArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
public class BlogArticleController {

    @Autowired
    private BlogArticleService blogArticleService;

    @GetMapping
    public ResponseEntity<List<BlogArticleSummaryDto>> listarPublicados() {
        return ResponseEntity.ok(blogArticleService.listarPublicados());
    }

    @GetMapping("/{idOuSlug}")
    public ResponseEntity<BlogArticleDetailDto> buscarPublicadoPorIdOuSlug(@PathVariable String idOuSlug) {
        try {
            return ResponseEntity.ok(blogArticleService.buscarPublicadoPorIdOuSlug(idOuSlug));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<List<BlogArticleSummaryDto>> listarParaAdmin() {
        return ResponseEntity.ok(blogArticleService.listarTodosParaAdmin());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<BlogArticleDetailDto> buscarParaAdmin(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(blogArticleService.buscarPorIdParaAdmin(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<BlogArticleDetailDto> criar(@RequestBody BlogArticleUpsertDto dto) {
        BlogArticleDetailDto created = blogArticleService.criar(dto);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogArticleDetailDto> atualizar(@PathVariable Integer id, @RequestBody BlogArticleUpsertDto dto) {
        try {
            return ResponseEntity.ok(blogArticleService.atualizar(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        blogArticleService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
