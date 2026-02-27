package com.signature.autosave.infra.components.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SMTPProvider implements EmailProvider {
    private final JavaMailSender mailSender;
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    @Value("${spring.mail.username}")
    private String email;

    public void sendEmail(String to, String subject, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(email);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(message);
    }

    public String buildTemplate(String name, String datetime, String topic, String title, String text1,
                                String text2, String buttonUrl) {
        return processTemplate(
                "/templates/template.html",
                Map.of(
                        "NAME", name,
                        "DATETIME", datetime,
                        "TOPIC", topic,
                        "TITLE", title,
                        "TEXT.1", text1,
                        "TEXT.2", text2,
                        "BUTTON_URL", buttonUrl
                )
        );
    }

    private String loadTemplate(String path) {
        return templateCache.computeIfAbsent(path, p -> {
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

    private String processTemplate(String path, Map<String, String> variables) {
        String template = loadTemplate(path);

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return template;
    }

}
