package com.signature.autosave.modules.outbox.service.events;

import com.signature.autosave.modules.email.content.service.events.EmailContentUpdatedEvent;

public record OutboxEmailContentUpdatedEvent(EmailContentUpdatedEvent emailContentUpdatedEvent) {
}
