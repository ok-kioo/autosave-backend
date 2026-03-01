package com.signature.autosave.modules.email.campaign.dto;

import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter

public class CreateEmailCampaignReviewDTO {
    @NotNull
    private EmailCampaignStatus status;

    @NotNull
    private String comment;

    @NotNull
    private UUID emailCampaign;

}
