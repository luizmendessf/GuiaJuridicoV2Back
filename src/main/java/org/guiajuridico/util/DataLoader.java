package org.guiajuridico.util;

import org.guiajuridico.model.Role;
import org.guiajuridico.model.Usuario;
import org.guiajuridico.repository.RoleRepository;
import org.guiajuridico.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Criar roles se não existirem
        criarRolesSeNaoExistirem();
        
        // Criar usuário admin se não existir
        criarUsuarioAdminSeNaoExistir();
    }

    private void criarRolesSeNaoExistirem() {
        if (roleRepository.findByNome("ROLE_USUARIO") == null) {
            Role roleUsuario = new Role();
            roleUsuario.setNome("ROLE_USUARIO");
            roleRepository.save(roleUsuario);
            log.info("Role {} criada.", "ROLE_USUARIO");
        }

        if (roleRepository.findByNome("ROLE_ORGANIZADOR") == null) {
            Role roleOrganizador = new Role();
            roleOrganizador.setNome("ROLE_ORGANIZADOR");
            roleRepository.save(roleOrganizador);
            log.info("Role {} criada.", "ROLE_ORGANIZADOR");
        }

        if (roleRepository.findByNome("ROLE_ADMIN") == null) {
            Role roleAdmin = new Role();
            roleAdmin.setNome("ROLE_ADMIN");
            roleRepository.save(roleAdmin);
            log.info("Role {} criada.", "ROLE_ADMIN");
        }
    }

    private void criarUsuarioAdminSeNaoExistir() {
        String emailAdmin = "admin@guiajuridico.com";
        
        if (usuarioRepository.findByEmail(emailAdmin).isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setEmail(emailAdmin);
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setCelular("(11) 99999-9999");
            
            // Atribuir roles de ADMIN e ORGANIZADOR
            Set<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByNome("ROLE_ADMIN"));
            roles.add(roleRepository.findByNome("ROLE_ORGANIZADOR"));
            roles.add(roleRepository.findByNome("ROLE_USUARIO"));
            admin.setRoles(roles);
            
            usuarioRepository.save(admin);
            log.info("Usuário admin criado: {}.", emailAdmin);
        }
    }
}