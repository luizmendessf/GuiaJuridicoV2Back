package org.guiajuridico.controller;

import org.guiajuridico.model.Oportunidade;
import org.guiajuridico.service.OportunidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/oportunidades")
public class OportunidadeController {

    @Autowired
    private OportunidadeService oportunidadeService;

    // ENDPOINT para listar todas as oportunidades
    @GetMapping("/todas")
    public ResponseEntity<List<Oportunidade>> listarTodasOportunidades() {
        List<Oportunidade> oportunidades = oportunidadeService.listarTodas();
        return ResponseEntity.ok(oportunidades);
    }

    // / Endpoint GET para aceitar parâmetros de filtro
    @GetMapping
    public ResponseEntity<List<Oportunidade>> listarOportunidades(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String termo) {

        List<Oportunidade> oportunidades = oportunidadeService.listarComFiltros(tipo, status, termo);
        return ResponseEntity.ok(oportunidades);
    }

    // Endpoint para CRIAR uma nova oportunidade
    @PostMapping
    public ResponseEntity<Oportunidade> criarOportunidade(@RequestBody Oportunidade oportunidade) {
        Oportunidade novaOportunidade = oportunidadeService.criarOportunidade(oportunidade);
        return ResponseEntity.status(201).body(novaOportunidade);
    }

    // Endpoint para ATUALIZAR uma oportunidade
    @PutMapping("/{id}")
    public ResponseEntity<Oportunidade> atualizarOportunidade(@PathVariable Integer id, @RequestBody Oportunidade oportunidade) {
        return oportunidadeService.atualizarOportunidade(id, oportunidade)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint para DELETAR uma oportunidade
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarOportunidade(@PathVariable Integer id) {
        oportunidadeService.deletarOportunidade(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint para BUSCAR uma oportunidade por ID
    @GetMapping("/{id}")
    public ResponseEntity<Oportunidade> buscarPorId(@PathVariable Integer id) {
        return oportunidadeService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}