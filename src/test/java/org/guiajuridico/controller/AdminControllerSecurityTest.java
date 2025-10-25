package org.guiajuridico.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.guiajuridico.GuiaJuridicoBackApplication;
import org.guiajuridico.model.Role;
import org.guiajuridico.model.Usuario;
import org.guiajuridico.service.JwtService;
import org.guiajuridico.service.UsuarioService;
import org.guiajuridico.util.DataLoader;
import org.guiajuridico.util.MigracaoOportunidades;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GuiaJuridicoBackApplication.class,
        properties = "spring.autoconfigure.exclude=" + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@AutoConfigureMockMvc
class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    // Disable startup runners that hit repositories
    @MockBean
    private DataLoader dataLoader;

    @MockBean
    private MigracaoOportunidades migracaoOportunidades;

    @Autowired
    private JwtService jwtService;

    @Value("${jwt.secret}")
    private String secretKeyBase64;

    private String buildToken(String username, List<String> authorities) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyBase64));
        return Jwts.builder()
                .subject(username)
                .claim("authorities", authorities)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
                .signWith(key)
                .compact();
    }

    private UserDetails buildUserDetails(String username, Collection<? extends GrantedAuthority> authorities) {
        return new User(username, "{noop}ignored", authorities);
    }

    @Test
    void put_roles_with_unprefixed_authority_claim_returns_403() throws Exception {
        String username = "admin@guiajuridico.com";
        // UserDetails returned from DB has ROLE_ prefix as expected
        UserDetails dbUser = buildUserDetails(username, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(userDetailsService.loadUserByUsername(eq(username))).thenReturn(dbUser);
        when(usuarioService.atualizarRolesUsuario(any(Integer.class), any(Set.class))).thenReturn(new Usuario());

        String badToken = buildToken(username, List.of("ADMIN")); // missing ROLE_ prefix

        String body = "{\"nomesDasRoles\":[\"ROLE_USUARIO\"]}";
        mockMvc.perform(put("/api/admin/usuarios/1/roles")
                        .header("Authorization", "Bearer " + badToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void put_roles_with_prefixed_authority_claim_returns_200() throws Exception {
        String username = "admin@guiajuridico.com";
        UserDetails dbUser = buildUserDetails(username, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(userDetailsService.loadUserByUsername(eq(username))).thenReturn(dbUser);

        Usuario dummy = new Usuario();
        dummy.setId(1);
        dummy.setNome("dummy");
        Set<Role> roles = new HashSet<>();
        Role userRole = new Role(); userRole.setNome("ROLE_USUARIO"); roles.add(userRole);
        dummy.setRoles(roles);
        when(usuarioService.atualizarRolesUsuario(eq(1), any(Set.class))).thenReturn(dummy);

        String goodToken = buildToken(username, List.of("ROLE_ADMIN"));

        String body = "{\"nomesDasRoles\":[\"ROLE_USUARIO\"]}";
        mockMvc.perform(put("/api/admin/usuarios/1/roles")
                        .header("Authorization", "Bearer " + goodToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());
    }
}