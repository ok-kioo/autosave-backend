package com.signature.autosave.modules.email.campaign.builder;

import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaignReview;
import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;
import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.validation.constraints.NotNull;

public class EmailCampaignReviewBuilder {
    private EmailCampaign emailCampaign;
    private EmailCampaignStatus status;
    private String comment;
    private User reviewer;

    private EmailCampaignReviewBuilder() {
    }

    public static EmailCampaignReviewBuilder builder() {
        return new EmailCampaignReviewBuilder();
    }

    public EmailCampaignReviewBuilder withEmailCampaign(@NotNull EmailCampaign emailCampaign) {
        this.emailCampaign = emailCampaign;
        return this;
    }

    public EmailCampaignReviewBuilder withStatus(@NotNull EmailCampaignStatus status) {
        this.status = status;
        return this;
    }

    public EmailCampaignReviewBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public EmailCampaignReviewBuilder withReviewer(User reviewer) {
        this.reviewer = reviewer;
        return this;
    }

    public EmailCampaignReview build() {
        EmailCampaignReview emailCampaign = new EmailCampaignReview();
        emailCampaign.setEmailCampaign(this.emailCampaign);
        emailCampaign.setStatus(this.status);
        emailCampaign.setComment(this.comment);
        emailCampaign.setReviewer(this.reviewer);

        return emailCampaign;
    }
}
