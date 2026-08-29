package com.signature.autosave.modules.email.campaign.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateEmailCampaignDTO (@NotNull
                                      UUID emailContent,

                                      @NotNull
                                      String textPreview){

}
