package com.signature.autosave.modules.user.service.events;

import java.util.UUID;

public record UserCreatedEvent(UUID user) {
}
