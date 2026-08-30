package com.signature.autosave.modules.email.content.service.events;

import java.util.UUID;

public record EmailContentDeletedEvent(UUID emailContentId) {
}
