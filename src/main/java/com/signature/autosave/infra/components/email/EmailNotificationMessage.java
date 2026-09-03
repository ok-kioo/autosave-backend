package com.signature.autosave.infra.components.email;

import java.util.UUID;

public record EmailNotificationMessage(
        UUID publishId,
        String to,
        String subject,
        String html
) {}
