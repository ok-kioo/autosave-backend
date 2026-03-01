package com.signature.autosave.modules.email.campaign.builder;

import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaignReview;
import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;
import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.validation.constraints.NotNull;

public class CommentNodeBuilder {
    private EmailCampaign emailCampaign;
    private EmailCampaignStatus status;
    private String comment;
    private User reviewer;

    private CommentNodeBuilder() {
    }

    public static CommentNodeBuilder builder() {
        return new CommentNodeBuilder();
    }

    public CommentNodeBuilder withEmailCampaign(@NotNull EmailCampaign emailCampaign) {
        this.emailCampaign = emailCampaign;
        return this;
    }

    public CommentNodeBuilder withStatus(@NotNull EmailCampaignStatus status) {
        this.status = status;
        return this;
    }

    public CommentNodeBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public CommentNodeBuilder withReviewer(User reviewer) {
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
