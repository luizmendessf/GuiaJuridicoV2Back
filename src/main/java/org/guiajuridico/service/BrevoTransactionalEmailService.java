package org.guiajuridico.service;

import org.guiajuridico.config.AppProperties;
import org.guiajuridico.config.BrevoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class BrevoTransactionalEmailService {

    private static final Logger log = LoggerFactory.getLogger(BrevoTransactionalEmailService.class);

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.brevo.com")
            .build();

    private final BrevoProperties brevoProperties;
    private final AppProperties appProperties;

    public BrevoTransactionalEmailService(BrevoProperties brevoProperties, AppProperties appProperties) {
        this.brevoProperties = brevoProperties;
        this.appProperties = appProperties;
    }

    public void sendPasswordResetEmail(String recipientEmail, String recipientName, String resetToken) {
        String resetUrl = appProperties.getFrontendUrl() + "/redefinir-senha?token=" + resetToken;
        String displayName = recipientName == null || recipientName.isBlank() ? "usuário" : recipientName;

        if (!brevoProperties.canSendTransactionalEmail()) {
            log.warn(
                    "E-mail de reset não enviado (configure BREVO_API_KEY e BREVO_SENDER_EMAIL). Link para {}: {}",
                    recipientEmail,
                    resetUrl
            );
            return;
        }

        String html = """
                <p>Olá, %s!</p>
                <p>Recebemos um pedido para redefinir a senha da sua conta no <strong>Guia Jurídico</strong>.</p>
                <p><a href="%s">Clique aqui para criar uma nova senha</a></p>
                <p>Ou copie e cole no navegador:<br/><a href="%s">%s</a></p>
                <p>Este link expira em <strong>1 hora</strong>. Se você não solicitou isso, ignore este e-mail.</p>
                <p>— Equipe Guia Jurídico</p>
                """.formatted(displayName, resetUrl, resetUrl, resetUrl);

        Map<String, Object> body = Map.of(
                "sender", Map.of(
                        "name", brevoProperties.getSenderName(),
                        "email", brevoProperties.getSenderEmail()
                ),
                "to", List.of(Map.of("email", recipientEmail, "name", displayName)),
                "subject", "Redefinir senha — Guia Jurídico",
                "htmlContent", html
        );

        try {
            restClient.post()
                    .uri("/v3/smtp/email")
                    .header("api-key", brevoProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("E-mail de redefinição de senha enviado para {}", recipientEmail);
        } catch (RestClientResponseException e) {
            log.error("Brevo POST /v3/smtp/email falhou: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("Não foi possível enviar o e-mail de redefinição de senha.", e);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail transacional Brevo", e);
            throw new IllegalStateException("Não foi possível enviar o e-mail de redefinição de senha.", e);
        }
    }
}
