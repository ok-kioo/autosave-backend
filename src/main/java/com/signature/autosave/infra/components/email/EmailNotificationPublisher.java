package com.signature.autosave.infra.components.email;

import com.signature.autosave.infra.configuration.RabbitMQConfig;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignSendEvent;
import com.signature.autosave.modules.email.content.service.events.EmailContentUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailNotificationPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    @Value("${app.frontend.url}")
    private String frontEndUrl;

    private String loadTemplate() {
        return templateCache.computeIfAbsent("/templates/template.html", p -> {
            try (InputStream is = getClass().getResourceAsStream(p)) {
                if (is == null) {
                    throw new IllegalArgumentException("Template not found: " + p);
                }
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Load template error", e);
            }
        });
    }

    private String processTemplate(Map<String, String> variables) {
        String template = loadTemplate();

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return template;
    }

    private String buildTemplate(String name, String datetime, String subject, String title, String previewText, String buttonUrl) {
        return processTemplate(
                Map.of(
                        "NAME", name,
                        "DATETIME", datetime,
                        "SUBJECT", subject,
                        "TITLE", title,
                        "PREVIEW_TEXT", previewText,
                        "BUTTON_URL", buttonUrl
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishUpdatedContentEmail(EmailContentUpdatedEvent emailContentUpdatedEvent){

        String template = this.buildTemplate(
                emailContentUpdatedEvent.emailContent().getEditor().getName(),
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                "Revisão de campanha de email",
                "Conteúdo de email atualizado",
                "O conteúdo do email '" + emailContentUpdatedEvent.emailContent().getSubject() + "', referente a campanha revisada por você foi atualizado. " +
                        "Por favor, revise as alterações e aprove ou rejeite a campanha de email associada.",
                frontEndUrl+"/email/content/review" + emailContentUpdatedEvent.emailCampaignReviewId()
        );

        this.publish(UUID.randomUUID(), EmailNotificationMessage.EmailType.TO,
                Collections.singletonList(emailContentUpdatedEvent.emailCampaignReviewerEmail()),
                "Revisão de campanha de email", template);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishSendEmailCampaign(EmailCampaignSendEvent emailCampaignSendEvent){

        String template = this.buildTemplate(
                emailCampaignSendEvent.emailContent().getEditor().getName(),
                emailCampaignSendEvent.emailContent().getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                emailCampaignSendEvent.emailContent().getTopic(),
                emailCampaignSendEvent.emailContent().getSubject(),
                emailCampaignSendEvent.textPreview(),
                frontEndUrl+"/email/content/" + emailCampaignSendEvent.emailCampaignId()
        );

        this.publish(UUID.randomUUID(), EmailNotificationMessage.EmailType.BCC, emailCampaignSendEvent.usersToSend(),
                "Revisão de campanha de email", template);
    }

    private void publish(UUID publishId, EmailNotificationMessage.EmailType emailType, List<String> recipients,
                         String subject, String html) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                new EmailNotificationMessage(
                        publishId,
                        emailType,
                        recipients,
                        subject,
                        html
                )
        );
    }

}
