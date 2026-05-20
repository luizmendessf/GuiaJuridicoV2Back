package org.guiajuridico.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    
    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Permite embed de PDFs/imagens no iframe do front (origens distintas em dev e produção).
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "frame-ancestors 'self' http://localhost:3000 http://127.0.0.1:3000 "
                                        + "http://localhost:5173 http://127.0.0.1:5173 "
                                        + "https://guiajuridico.org https://www.guiajuridico.org"
                        ))
                )
                .authorizeHttpRequests(authorize -> authorize
                        // --- REGRAS ESPECÍFICAS PRIMEIRO ---
                        // Rotas Públicas: Todos podem acessar o login/registro e ver as vagas.
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/oportunidades/**").permitAll()
                        .requestMatchers("/api/blog/admin/**").hasAnyRole("ORGANIZADOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/blog").hasAnyRole("ORGANIZADOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/blog/**").hasAnyRole("ORGANIZADOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/blog/**").hasAnyRole("ORGANIZADOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/blog/**").permitAll()
                        // Biblioteca: GET público (lista + detalhe); /admin e mutações exigem ORGANIZADOR/ADMIN
                        // Incluir /admin sem segmento extra (alguns PathPatterns não casam só com /admin/**).
                        .requestMatchers("/api/biblioteca/admin", "/api/biblioteca/admin/**").hasAnyRole("ORGANIZADOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/biblioteca").hasAnyRole("ORGANIZADOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/biblioteca/**").hasAnyRole("ORGANIZADOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/biblioteca/**").hasAnyRole("ORGANIZADOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/biblioteca", "/api/biblioteca/**").permitAll()
                        // Biblioteca: GET público; upload de PDF no mesmo modelo que imagens (permitAll no arquivo).
                        // Quem pode usar o fluxo fica na UI (botão / rascunhos); criar/editar documento continua com hasRole.
                        .requestMatchers("/api/pdfs/**").permitAll()
                        .requestMatchers("/api/images/**").permitAll()

                        // Newsletter, webhook Brevo e demais rotas públicas sob /api/public/
                        .requestMatchers("/api/public/**").permitAll()

                        // Rotas de Usuário Autenticado: Qualquer usuário logado pode gerenciar seus favoritos.
                        .requestMatchers("/api/usuarios/me/**").authenticated()

                        // Rotas de Organizador/Admin: Apenas usuários com essas permissões podem gerenciar vagas.
                        .requestMatchers(HttpMethod.POST, "/api/oportunidades").hasAnyRole("ORGANIZADOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/oportunidades/**").hasAnyRole("ORGANIZADOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/oportunidades/**").hasAnyRole("ORGANIZADOR", "ADMIN")

                        // Apenas usuários com a permissão ADMIN podem acessar rotas /api/admin/**
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // --- REGRA GERAL POR ÚLTIMO ---
                        // Qualquer outra requisição que não foi definida acima exige autenticação.
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                // JWT antes do anónimo para o Bearer substituir o contexto anónimo nas rotas protegidas.
                .addFilterBefore(jwtAuthFilter, AnonymousAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "https://guiajuridico.org",
                "https://www.guiajuridico.org"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
