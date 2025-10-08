package org.guiajuridico.controller;

import org.guiajuridico.dto.CadastroUsuarioDto;
import org.guiajuridico.dto.LoginRequestDto;
import org.guiajuridico.dto.LoginResponseDto;
import org.guiajuridico.model.Usuario;
import org.guiajuridico.repository.UsuarioRepository;
import org.guiajuridico.service.JwtService;
import org.guiajuridico.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.guiajuridico.dto.ForgotPasswordRequestDto;
import org.guiajuridico.dto.ResetPasswordRequestDto;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> registrarUsuario(@RequestBody CadastroUsuarioDto cadastroDto) {
        // Converte o DTO para a entidade Usuario
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(cadastroDto.getNome());
        novoUsuario.setEmail(cadastroDto.getEmail());
        novoUsuario.setSenha(cadastroDto.getSenha());
        novoUsuario.setCelular(cadastroDto.getCelular());

        // Chama o serviço para criar o usuário (que já tem a lógica de criptografia e roles)
        usuarioService.criarUsuario(novoUsuario);

        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    //ENDPOINT DE LOGIN
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginDto) {
        // Autentica o usuário
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getSenha())
        );

        // Se a autenticação for bem-sucedida, busca o usuário e gera o token
        var usuario = usuarioRepository.findByEmail(loginDto.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(usuario);

        // Retorna o token
        return ResponseEntity.ok(new LoginResponseDto(jwtToken));
    }

    //Solicitar redefinição de senha
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequestDto request) {
        try {
            usuarioService.gerarResetToken(request.getEmail());
        } catch (RuntimeException e) {
            // Captura a exceção "Usuário não encontrado", mas não faz nada com ela.
            // O objetivo é que o fluxo continue e a mesma mensagem seja enviada.
        }
        // Retorna sempre a mesma mensagem genérica, independentemente de o email existir ou não
        return ResponseEntity.ok("Se um usuário com este email existir, um link de redefinição foi enviado.");
    }

    // Efetivar a redefinição de senha
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        usuarioService.resetarSenha(request.getToken(), request.getNovaSenha());
        return ResponseEntity.ok("Senha redefinida com sucesso!");
    }
}