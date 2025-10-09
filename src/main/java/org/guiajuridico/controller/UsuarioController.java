package org.guiajuridico.controller;

import org.guiajuridico.dto.SenhaUpdateRequestDto;
import org.guiajuridico.dto.UsuarioUpdateDto;
import org.guiajuridico.model.Oportunidade;
import org.guiajuridico.model.Usuario;
import org.guiajuridico.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // ENDPOINT PARA OBTER DADOS DO USUÁRIO LOGADO
    @GetMapping("/me")
    public ResponseEntity<Usuario> obterDadosUsuario(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/me/favoritos")
    public ResponseEntity<Set<Oportunidade>> listarFavoritos(@AuthenticationPrincipal UserDetails userDetails) {
        Set<Oportunidade> favoritos = usuarioService.listarFavoritos(userDetails.getUsername());
        return ResponseEntity.ok(favoritos);
    }

    @PostMapping("/me/favoritos/{oportunidadeId}")
    public ResponseEntity<Void> adicionarFavorito(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer oportunidadeId) {
        usuarioService.adicionarFavorito(userDetails.getUsername(), oportunidadeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me/favoritos/{oportunidadeId}")
    public ResponseEntity<Void> removerFavorito(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer oportunidadeId) {
        usuarioService.removerFavorito(userDetails.getUsername(), oportunidadeId);
        return ResponseEntity.noContent().build();
    }

    // NOVO ENDPOINT PARA MUDAR A SENHA
    @PostMapping("/me/mudar-senha")
    public ResponseEntity<String> mudarSenha(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SenhaUpdateRequestDto senhaDto) {

        usuarioService.mudarSenha(userDetails.getUsername(), senhaDto.getSenhaAntiga(), senhaDto.getSenhaNova());
        return ResponseEntity.ok("Senha alterada com sucesso!");
    }

    // ENDPOINT PARA ATUALIZAR DADOS DO USUÁRIO
    @PutMapping("/me")
    public ResponseEntity<Usuario> atualizarDadosUsuario(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UsuarioUpdateDto updateDto) {
        
        Usuario usuarioAtualizado = usuarioService.atualizarDadosUsuario(userDetails.getUsername(), updateDto);
        return ResponseEntity.ok(usuarioAtualizado);
    }
}