package com.signature.autosave.modules.email.campaign.dto;

import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateEmailCampaignReviewDTO (@NotNull
                                            EmailCampaignStatus status,

                                            @NotNull
                                            String comment,

                                            @NotNull
                                            UUID emailCampaign){

}
