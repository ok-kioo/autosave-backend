package com.signature.autosave.modules.email.campaign.dto;

import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EmailCampaignResponseDTO (UUID id,

                                        String textPreview,

                                        EmailContent emailContent,

                                        List<SubscriptionPlan> subscriptionPlans,

                                        boolean isAvailable,

                                        LocalDateTime createdAt) {
}
