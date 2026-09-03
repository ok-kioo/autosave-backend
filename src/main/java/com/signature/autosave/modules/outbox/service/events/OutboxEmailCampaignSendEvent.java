package com.signature.autosave.modules.outbox.service.events;

import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignSendEvent;

public record OutboxEmailCampaignSendEvent(EmailCampaignSendEvent emailCampaignSendEvent) {
}
