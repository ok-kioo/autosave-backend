package com.signature.autosave.modules.email.campaign.service.events;

import com.signature.autosave.modules.email.content.domain.entity.EmailContent;

import java.util.List;
import java.util.UUID;

public record EmailCampaignSendEvent(UUID emailCampaignId, EmailContent emailContent, String textPreview, List<String> usersToSend) {
}
