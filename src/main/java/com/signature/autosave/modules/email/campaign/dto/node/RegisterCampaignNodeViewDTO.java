package com.signature.autosave.modules.email.campaign.dto.node;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter

public class RegisterCampaignNodeViewDTO {
    @NotNull
    private UUID userId;

    @NotNull
    private UUID emailCampaignId;

}
