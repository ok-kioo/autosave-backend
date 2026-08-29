package com.signature.autosave.modules.email.campaign.dto;

import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;

public record UpdateEmailCampaignReviewDTO (EmailCampaignStatus status,

                                            String comment) {
}
