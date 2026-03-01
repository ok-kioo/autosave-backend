package com.signature.autosave.modules.email.campaign.dto;

import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;

import java.util.UUID;

public class EmailCampaignReviewResponseDTO {
    private UUID id;

    private EmailCampaignStatus status;

    private String comment;

    private EmailCampaign emailCampaign;

    public EmailCampaignReviewResponseDTO(UUID id, EmailCampaignStatus status, String comment, EmailCampaign emailCampaign) {
        this.id = id;
        this.status = status;
        this.comment = comment;
        this.emailCampaign = emailCampaign;
    }
}
