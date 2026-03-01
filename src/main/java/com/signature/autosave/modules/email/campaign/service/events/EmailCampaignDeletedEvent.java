package com.signature.autosave.modules.email.campaign.service.events;

import java.util.UUID;

public record EmailCampaignDeletedEvent(UUID emailCampaign) {
}
