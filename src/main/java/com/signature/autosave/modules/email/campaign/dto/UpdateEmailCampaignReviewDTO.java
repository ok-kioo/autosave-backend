package com.signature.autosave.modules.email.campaign.dto;

import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;
import lombok.Getter;

@Getter

public class UpdateEmailCampaignReviewDTO {
    private EmailCampaignStatus status;

    private String comment;
}
