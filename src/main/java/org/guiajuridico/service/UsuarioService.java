package org.guiajuridico.service;

import org.guiajuridico.dto.GoogleUserInfo;
import org.guiajuridico.dto.UsuarioUpdateDto;
import org.guiajuridico.model.Oportunidade;
import org.guiajuridico.model.Role;
import org.guiajuridico.model.Usuario;
import org.guiajuridico.repository.OportunidadeRepository;
import org.guiajuridico.repository.RoleRepository;
import org.guiajuridico.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UsuarioService {
    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // LINHA ADICIONADA PARA CORRIGIR O ERRO
    @Autowired
    private OportunidadeRepository oportunidadeRepository;

    @Autowired
    private BrevoTransactionalEmailService brevoTransactionalEmailService;

    public Usuario autenticarOuRegistrarComGoogle(GoogleUserInfo googleUser) {
        return usuarioRepository.findByGoogleId(googleUser.googleId())
                .orElseGet(() -> vincularOuCriarUsuarioGoogle(googleUser));
    }

    private Usuario vincularOuCriarUsuarioGoogle(GoogleUserInfo googleUser) {
        var existente = usuarioRepository.findByEmail(googleUser.email());
        if (existente.isPresent()) {
            Usuario usuario = existente.get();
            if (usuario.getGoogleId() != null && !usuario.getGoogleId().equals(googleUser.googleId())) {
                throw new RuntimeException("Este email já está vinculado a outra conta Google.");
            }
            usuario.setGoogleId(googleUser.googleId());
            return usuarioRepository.save(usuario);
        }

        Usuario novo = new Usuario();
        novo.setNome(googleUser.name());
        novo.setEmail(googleUser.email());
        novo.setGoogleId(googleUser.googleId());
        novo.setSenha(null);

        Role userRole = roleRepository.findByNome("ROLE_USUARIO");
        novo.setRoles(new HashSet<>(Collections.singletonList(userRole)));
        return usuarioRepository.save(novo);
    }

    public Usuario criarUsuario(Usuario usuario) {
        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória no cadastro.");
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha().trim()));

        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            // Lógica de Negócio 2: Atribuir a permissão padrão "ROLE_USUARIO"
            Role userRole = roleRepository.findByNome("ROLE_USUARIO");
            usuario.setRoles(new HashSet<>(Collections.singletonList(userRole)));
        }

        // Salva o novo usuário no banco de dados
        return usuarioRepository.save(usuario);
    }

    public void adicionarFavorito(String emailUsuario, Integer oportunidadeId) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElseThrow();
        Oportunidade oportunidade = oportunidadeRepository.findById(oportunidadeId).orElseThrow();

        usuario.getOportunidadesSalvas().add(oportunidade);
        usuarioRepository.save(usuario);
    }

    public Set<Oportunidade> listarFavoritos(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElseThrow();
        return usuario.getOportunidadesSalvas();
    }

    public void removerFavorito(String emailUsuario, Integer oportunidadeId) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElseThrow();
        Oportunidade oportunidade = oportunidadeRepository.findById(oportunidadeId).orElseThrow();

        usuario.getOportunidadesSalvas().remove(oportunidade);
        usuarioRepository.save(usuario);
    }

    public Usuario atualizarRolesUsuario(Integer usuarioId, Set<String> nomesDasRoles) {
        // Busca o usuário que será modificado
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        // Busca todas as roles correspondentes aos nomes fornecidos
        Set<Role> novasRoles = nomesDasRoles.stream()
                .map(nomeRole -> {
                    Role role = roleRepository.findByNome(nomeRole);
                    if (role == null) {
                        log.warn("Role não encontrada: {}", nomeRole);
                    }
                    return role;
                })
                .filter(r -> r != null)
                .collect(Collectors.toSet());

        // Define as novas roles para o usuário
        usuario.setRoles(novasRoles);

        // Salva o usuário atualizado
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));
    }

    public void deletarUsuario(Integer id) {
        // Lógica de segurança para impedir que um admin se auto-delete
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario adminLogado = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        if (adminLogado.getId().equals(id)) {
            throw new RuntimeException("Um administrador não pode apagar a própria conta.");
        }

        usuarioRepository.deleteById(id);
    }

    public void mudarSenha(String emailUsuario, String senhaAntiga, String senhaNova) {
        // 1. Busca o usuário no banco de dados
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        // 2. Verifica se a senha antiga fornecida corresponde à senha salva (criptografada)
        if (!passwordEncoder.matches(senhaAntiga, usuario.getPassword())) {
            throw new RuntimeException("Senha antiga incorreta!");
        }

        // 3. Criptografa e define a nova senha
        usuario.setSenha(passwordEncoder.encode(senhaNova));

        // 4. Salva o usuário com a senha atualizada
        usuarioRepository.save(usuario);
    }

    public Usuario atualizarDadosUsuario(String emailUsuario, UsuarioUpdateDto updateDto) {
        // 1. Busca o usuário no banco de dados
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        // 2. Atualiza os dados básicos (nome e celular)
        if (updateDto.getNome() != null && !updateDto.getNome().trim().isEmpty()) {
            usuario.setNome(updateDto.getNome().trim());
        }
        
        if (updateDto.getCelular() != null) {
            usuario.setCelular(updateDto.getCelular().trim());
        }

        // 3. Se uma nova senha foi fornecida, valida a senha atual e atualiza
        if (updateDto.getSenhaNova() != null && !updateDto.getSenhaNova().trim().isEmpty()) {
            if (updateDto.getSenhaAtual() == null || updateDto.getSenhaAtual().trim().isEmpty()) {
                throw new RuntimeException("Senha atual é obrigatória para alterar a senha!");
            }
            
            if (!passwordEncoder.matches(updateDto.getSenhaAtual(), usuario.getPassword())) {
                throw new RuntimeException("Senha atual incorreta!");
            }
            
            usuario.setSenha(passwordEncoder.encode(updateDto.getSenhaNova()));
        }

        // 4. Salva o usuário atualizado
        return usuarioRepository.save(usuario);
    }

    // Gerar token para recuperação de senha
    public String gerarResetToken(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o email: " + email));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // Token válido por 1 hora

        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordTokenExpiry(expiryDate);

        usuarioRepository.save(usuario);

        brevoTransactionalEmailService.sendPasswordResetEmail(
                usuario.getEmail(),
                usuario.getNome(),
                token
        );

        return token;
    }

    // NOVO MÉTODO: Redefinir a senha usando o token
    public void resetarSenha(String token, String novaSenha) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token inválido ou não encontrado.");
        }
        if (novaSenha == null || novaSenha.isBlank()) {
            throw new IllegalArgumentException("Nova senha é obrigatória.");
        }

        Usuario usuario = usuarioRepository.findByResetPasswordToken(token.trim())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou não encontrado."));

        if (usuario.getResetPasswordTokenExpiry() == null
                || usuario.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expirado. Solicite um novo link.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha.trim()));

        // Limpa os campos do token de redefinição
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpiry(null);

        usuarioRepository.save(usuario);
    }
}