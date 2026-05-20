package org.guiajuridico.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.guiajuridico.dto.GoogleUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class GoogleTokenVerifierService {

    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final String clientId;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GoogleTokenVerifierService(
            @Value("${google.oauth.client-id:}") String clientId,
            ObjectMapper objectMapper) {
        this.clientId = clientId == null ? "" : clientId.trim();
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public GoogleUserInfo verify(String idToken) {
        if (clientId.isEmpty()) {
            throw new IllegalStateException("Login com Google não está configurado no servidor.");
        }
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Token do Google ausente.");
        }

        try {
            String encoded = URLEncoder.encode(idToken.trim(), StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_INFO_URL + encoded))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalArgumentException("Token do Google inválido ou expirado.");
            }

            JsonNode payload = objectMapper.readTree(response.body());
            String aud = textOrEmpty(payload, "aud");
            if (!clientId.equals(aud)) {
                throw new IllegalArgumentException("Token do Google não pertence a este aplicativo.");
            }

            String emailVerified = textOrEmpty(payload, "email_verified");
            if (!"true".equalsIgnoreCase(emailVerified)) {
                throw new IllegalArgumentException("Email do Google não verificado.");
            }

            String googleId = textOrEmpty(payload, "sub");
            String email = textOrEmpty(payload, "email");
            String name = textOrEmpty(payload, "name");

            if (googleId.isEmpty() || email.isEmpty()) {
                throw new IllegalArgumentException("Dados insuficientes no token do Google.");
            }

            if (name.isEmpty()) {
                name = email;
            }

            return new GoogleUserInfo(googleId, email, name);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Não foi possível validar o token do Google.", e);
        }
    }

    private static String textOrEmpty(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText("").trim();
    }
}
