package org.guiajuridico.controller;

import org.guiajuridico.dto.CadastroUsuarioDto;
import org.guiajuridico.dto.GoogleAuthRequestDto;
import org.guiajuridico.dto.LoginRequestDto;
import org.guiajuridico.dto.LoginResponseDto;
import org.guiajuridico.model.Usuario;
import org.guiajuridico.repository.UsuarioRepository;
import org.guiajuridico.service.GoogleTokenVerifierService;
import org.guiajuridico.service.JwtService;
import org.guiajuridico.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    private GoogleTokenVerifierService googleTokenVerifierService;

    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@RequestBody CadastroUsuarioDto cadastroDto) {
        try {
            Usuario novoUsuario = new Usuario();
            novoUsuario.setNome(cadastroDto.getNome());
            novoUsuario.setEmail(cadastroDto.getEmail());
            novoUsuario.setSenha(cadastroDto.getSenha());
            novoUsuario.setCelular(cadastroDto.getCelular());

            usuarioService.criarUsuario(novoUsuario);
            return ResponseEntity.ok("Usuário registrado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

    @PostMapping("/google")
    public ResponseEntity<?> loginComGoogle(@RequestBody GoogleAuthRequestDto request) {
        try {
            var googleUser = googleTokenVerifierService.verify(request.getCredential());
            var usuario = usuarioService.autenticarOuRegistrarComGoogle(googleUser);
            var jwtToken = jwtService.generateToken(usuario);
            return ResponseEntity.ok(new LoginResponseDto(jwtToken));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
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