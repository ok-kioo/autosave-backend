package com.signature.autosave.modules.email.campaign.dto.node;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record RegisterCampaignNodeViewDTO(@NotNull
                                          UUID userId,

                                          @NotNull
                                          UUID emailCampaignId) {


}
