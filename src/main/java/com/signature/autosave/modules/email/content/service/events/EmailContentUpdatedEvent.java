package com.signature.autosave.modules.email.content.service.events;

import com.signature.autosave.modules.email.content.domain.entity.EmailContent;

import java.util.UUID;

public record EmailContentUpdatedEvent(EmailContent emailContent, UUID emailCampaignReviewId, String emailCampaignReviewerEmail) {
}
