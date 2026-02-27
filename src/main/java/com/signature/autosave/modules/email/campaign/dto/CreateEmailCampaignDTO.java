package com.signature.autosave.modules.email.campaign.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter

public class CreateEmailCampaignDTO {
    @NotNull
    private UUID emailContent;

    @NotNull
    private String textPreview;

}
