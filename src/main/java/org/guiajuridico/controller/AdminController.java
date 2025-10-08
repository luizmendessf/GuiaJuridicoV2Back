package org.guiajuridico.controller;

import org.guiajuridico.dto.RoleUpdateRequestDto;
import org.guiajuridico.dto.UsuarioDto;
import org.guiajuridico.model.Usuario;
import org.guiajuridico.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    // ENDPOINT PARA QUE OERE ROLES DE USUÁRIOS
    @PutMapping("/usuarios/{id}/roles")
    public ResponseEntity<Usuario> atualizarRoles(
            @PathVariable Integer id,
            @RequestBody RoleUpdateRequestDto requestDto) {

        Usuario usuarioAtualizado = usuarioService.atualizarRolesUsuario(id, requestDto.getNomesDasRoles());
        return ResponseEntity.ok(usuarioAtualizado);
    }

    // ENDPOINT PARA LISTAR USUÁRIOS
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDto>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.listarTodosUsuarios();

        // Converte a lista de Entidades para uma lista de DTOs para segurança
        List<UsuarioDto> usuarioDtos = usuarios.stream().map(usuario -> {
            UsuarioDto dto = new UsuarioDto();
            dto.setId(usuario.getId());
            dto.setNome(usuario.getNome());
            dto.setEmail(usuario.getEmail());
            // Não incluímos a senha ou outros dados sensíveis
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(usuarioDtos);
    }

    // ENDPOINT PARA DELETAR UM USUÁRIO
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Integer id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}