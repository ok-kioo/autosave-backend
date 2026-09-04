package com.signature.autosave.infra.components.email;

import java.util.List;
import java.util.UUID;

public record EmailNotificationMessage(
        UUID publishId,
        EmailType type,
        List<String> recipients,
        String subject,
        String html
) {

    public enum EmailType {
        TO,
        BCC
    }
}


