package com.signature.autosave.modules.email.campaign.domain.entity;

import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "email_campaign")
public class EmailCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    private String textPreview;

    @JoinColumn(name = "email_content_id", referencedColumnName = "id")
    @OneToOne
    private EmailContent emailContent;

    @ManyToMany
    @JoinTable(
            name = "email_campaign_subscription_plan",
            joinColumns = @JoinColumn(name = "email_campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "subscription_plan_id")
    )
    private List<SubscriptionPlan> subscriptionPlans;

    @NotNull
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = false;

    @NotNull
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "disabled_at")
    private boolean disabledAt;

}
