package com.signature.autosave.modules.email.record.domain.entity;

import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @JoinColumn(name = "email_content_id", referencedColumnName = "id")
    @OneToOne
    private EmailContent emailContent;

    @ManyToMany
    @JoinTable(
            name = "email_campaign_subscription_plan",
            joinColumns = @JoinColumn(name = "email_campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "subscription_plan_id")
    )
    private List<SubscriptionPlan> subscriptionPlan;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;


}
