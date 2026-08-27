package com.signature.autosave.modules.subscription.domain.entity;

import com.signature.autosave.modules.subscription.domain.enums.BillingCycle;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "subscription_plan")
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @Column(unique = true)
    private String name;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    private String description;

    @NotNull
    @Min(0)
    @Max(365)
    private Integer trialDays;

    @NotNull
    @Column(name = "preapproval_plan_id", nullable = false, updatable = false, unique = true)
    private String preapprovalPlanId;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull
    @Column(name ="is_active", nullable = false)
    private Boolean isActive = true;

}
