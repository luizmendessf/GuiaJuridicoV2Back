package org.guiajuridico.service;

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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // LINHA ADICIONADA PARA CORRIGIR O ERRO
    @Autowired
    private OportunidadeRepository oportunidadeRepository;

    public Usuario criarUsuario(Usuario usuario) {
        // Lógica de Negócio 1: Criptografar a senha
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

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
                .map(nomeRole -> roleRepository.findByNome(nomeRole))
                .collect(Collectors.toSet());

        // Define as novas roles para o usuário
        usuario.setRoles(novasRoles);

        // Salva o usuário atualizado
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarTodosUsuarios() {
        return usuarioRepository.findAll();
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

    // Gerar token para recuperação de senha
    public String gerarResetToken(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o email: " + email));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // Token válido por 1 hora

        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordTokenExpiry(expiryDate);

        usuarioRepository.save(usuario);

        // Em uma aplicação real, aqui você enviaria o email com o token
        System.out.println("Token de reset para " + email + ": " + token);

        return token;
    }

    // NOVO MÉTODO: Redefinir a senha usando o token
    public void resetarSenha(String token, String novaSenha) {
        // Busca o usuário pelo token de redefinição
        Usuario usuario = usuarioRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido ou não encontrado."));

        // Verifica se o token não expirou
        if (usuario.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado!");
        }

        // Criptografa e define a nova senha
        usuario.setSenha(passwordEncoder.encode(novaSenha));

        // Limpa os campos do token de redefinição
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpiry(null);

        usuarioRepository.save(usuario);
    }
}