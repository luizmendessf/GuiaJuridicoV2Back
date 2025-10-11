package org.guiajuridico.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        // --- REGRAS ESPECÍFICAS PRIMEIRO ---
                        // Rotas Públicas: Todos podem acessar o login/registro e ver as vagas.
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/oportunidades/**").permitAll()
                        .requestMatchers("/api/images/**").permitAll()

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
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Origens permitidas parametrizadas via property cors.allowed-origins
        String originsProp = System.getProperty("cors.allowed-origins", System.getenv().getOrDefault("CORS_ALLOWED_ORIGINS", "https://guiajuridico.org,https://www.guiajuridico.org,http://localhost:3000,http://127.0.0.1:3000"));
        List<String> allowedOrigins = Arrays.stream(originsProp.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}