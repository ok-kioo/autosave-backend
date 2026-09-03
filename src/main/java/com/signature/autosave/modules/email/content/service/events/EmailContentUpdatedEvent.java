package com.signature.autosave.modules.email.content.service.events;

import com.signature.autosave.modules.email.content.domain.entity.EmailContent;

import java.util.UUID;

public record EmailContentUpdatedEvent(UUID eventId, EmailContent emailContent, UUID emailCampaignReviewId,
                                       String emailCampaignReviewerEmail) {
}
