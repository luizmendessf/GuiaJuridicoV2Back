package org.guiajuridico.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.guiajuridico.config.BrevoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BrevoContactsClient {

    private static final Logger log = LoggerFactory.getLogger(BrevoContactsClient.class);

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.brevo.com")
            .build();

    private final BrevoProperties brevoProperties;
    private final ObjectMapper objectMapper;

    public BrevoContactsClient(BrevoProperties brevoProperties, ObjectMapper objectMapper) {
        this.brevoProperties = brevoProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * Cria ou atualiza o contato e associa à lista da newsletter.
     *
     * @return id do contato no Brevo, ou vazio se a API key não estiver configurada (dev).
     */
    public Optional<Long> createOrUpdateContactOnNewsletterList(String email) {
        if (!brevoProperties.hasApiKey()) {
            log.warn("BREVO_API_KEY ausente: sincronização com Brevo ignorada para {}", email);
            return Optional.empty();
        }
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("listIds", List.of(brevoProperties.getNewsletterListId()));
        body.put("updateEnabled", true);

        try {
            String responseJson = restClient.post()
                    .uri("/v3/contacts")
                    .header("api-key", brevoProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            if (responseJson != null && !responseJson.isBlank()) {
                JsonNode root = objectMapper.readTree(responseJson);
                if (root.has("id") && root.get("id").canConvertToLong()) {
                    return Optional.of(root.get("id").longValue());
                }
            }
        } catch (RestClientResponseException e) {
            log.error("Brevo POST /v3/contacts falhou: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("Falha ao sincronizar com o provedor de e-mail.", e);
        } catch (Exception e) {
            log.error("Erro ao chamar Brevo POST /v3/contacts", e);
            throw new IllegalStateException("Falha ao sincronizar com o provedor de e-mail.", e);
        }

        return fetchContactIdByEmail(email);
    }

    public void removeEmailFromNewsletterList(String email) {
        if (!brevoProperties.hasApiKey()) {
            return;
        }
        Map<String, Object> body = Map.of("emails", List.of(email));
        long listId = brevoProperties.getNewsletterListId();
        try {
            restClient.post()
                    .uri("/v3/contacts/lists/{listId}/contacts/remove", listId)
                    .header("api-key", brevoProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.info("Brevo remove lista: contato já ausente ou e-mail desconhecido ({})", email);
                return;
            }
            log.warn("Brevo remove lista falhou: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.warn("Erro ao remover e-mail da lista Brevo", e);
        }
    }

    private Optional<Long> fetchContactIdByEmail(String email) {
        try {
            String path = "/v3/contacts/" + UriUtils.encodePathSegment(email, StandardCharsets.UTF_8);
            String responseJson = restClient.get()
                    .uri(URI.create(path))
                    .header("api-key", brevoProperties.getApiKey())
                    .retrieve()
                    .body(String.class);
            if (responseJson == null) {
                return Optional.empty();
            }
            JsonNode root = objectMapper.readTree(responseJson);
            if (root.has("id") && root.get("id").canConvertToLong()) {
                return Optional.of(root.get("id").longValue());
            }
        } catch (Exception e) {
            log.debug("Não foi possível obter id Brevo por e-mail: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
