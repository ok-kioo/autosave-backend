package com.signature.autosave.modules.email.campaign.dto;

import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;

import java.util.List;
import java.util.UUID;

public class EmailCampaignResponseDTO {
    private UUID id;

    private String textPreview;

    private EmailContent emailContent;

    private List<SubscriptionPlan> subscriptionPlans;

    private boolean isActive;

    public EmailCampaignResponseDTO(UUID id, String textPreview, EmailContent emailContent, List<SubscriptionPlan> subscriptionPlans, boolean isActive) {
        this.id = id;
        this.textPreview = textPreview;
        this.emailContent = emailContent;
        this.subscriptionPlans = subscriptionPlans;
        this.isActive = isActive;
    }
}
