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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

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
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        if (email.isBlank()) {
            return ResponseEntity.badRequest().body("Informe um e-mail válido.");
        }
        try {
            usuarioService.gerarResetToken(email);
        } catch (RuntimeException e) {
            // E-mail inexistente: mesma resposta genérica (não revelar cadastro).
        } catch (IllegalStateException e) {
            log.error("Falha ao enviar e-mail de reset para {}", email, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Não foi possível enviar o e-mail agora. Tente novamente em alguns minutos.");
        }
        return ResponseEntity.ok("Se um usuário com este email existir, um link de redefinição foi enviado.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        try {
            usuarioService.resetarSenha(request.getToken(), request.getNovaSenha());
            return ResponseEntity.ok("Senha redefinida com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}