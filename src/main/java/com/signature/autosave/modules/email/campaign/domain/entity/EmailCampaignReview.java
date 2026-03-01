package com.signature.autosave.modules.email.campaign.domain.entity;

import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;
import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "email_campaign_review")
public class EmailCampaignReview {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EmailCampaignStatus status;

    @NotNull
    private String comment;

    @JoinColumn(name = "email_campaign_id", referencedColumnName = "id")
    @ManyToOne
    private EmailCampaign emailCampaign;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", referencedColumnName = "id")
    private User reviewer;

}
