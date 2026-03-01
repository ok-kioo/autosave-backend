package com.signature.autosave.modules.email.campaign.builder;

import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class EmailCampaignBuilder {
    private EmailContent emailContent;
    private List<SubscriptionPlan> subscriptionPlans;
    private String textPreview;

    private EmailCampaignBuilder() {
    }

    public static EmailCampaignBuilder builder() {
        return new EmailCampaignBuilder();
    }

    public EmailCampaignBuilder withEmailContent(@NotNull EmailContent emailContent) {
        this.emailContent = emailContent;
        return this;
    }

    public EmailCampaignBuilder withSubscriptionPlans(@NotNull List<SubscriptionPlan> subscriptionPlans) {
        this.subscriptionPlans = subscriptionPlans;
        return this;
    }

    public EmailCampaignBuilder withTextPreview(@NotNull String textPreview) {
        this.textPreview = textPreview;
        return this;
    }

    public EmailCampaign build() {
        EmailCampaign emailCampaign = new EmailCampaign();
        emailCampaign.setTextPreview(this.textPreview);
        emailCampaign.setEmailContent(this.emailContent);
        emailCampaign.setSubscriptionPlans(this.subscriptionPlans);
        emailCampaign.setAvailable(false);
        return emailCampaign;
    }
}
