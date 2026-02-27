package com.signature.autosave.infra.components.email;

import jakarta.mail.MessagingException;

public interface EmailProvider {
    void sendEmail(String to, String subject, String html) throws MessagingException;

    String buildTemplate(String name, String datetime, String topic, String title, String text1,
                         String text2, String buttonUrl);

}
