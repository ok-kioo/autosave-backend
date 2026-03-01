package com.signature.autosave.modules.subscription.dto;

import com.signature.autosave.modules.subscription.domain.enums.BillingCycle;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SubscriptionPlanResponseDTO {
    @NotNull
    private UUID id;

    @NotNull
    private String name;

    @NotNull
    private BigDecimal price;

    @NotNull
    private BillingCycle billingCycle;

    @NotNull
    private Integer trialDays;

    @NotNull
    private LocalDateTime createdAt;

    public SubscriptionPlanResponseDTO(UUID id, String name, BigDecimal price, BillingCycle billingCycle, Integer trialDays, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.billingCycle = billingCycle;
        this.trialDays = trialDays;
        this.createdAt = createdAt;
    }
}
