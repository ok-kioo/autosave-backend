package com.signature.autosave.modules.email.campaign.dto;

import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;

import java.util.UUID;

public record EmailCampaignReviewResponseDTO (UUID id,

                                             EmailCampaignStatus status,

                                             String comment,

                                             EmailCampaign emailCampaign) {

}
