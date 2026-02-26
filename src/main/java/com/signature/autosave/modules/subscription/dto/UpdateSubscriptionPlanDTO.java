package com.signature.autosave.modules.subscription.dto;

import com.signature.autosave.modules.subscription.domain.enums.BillingCycle;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UpdateSubscriptionPlanDTO {
    private String name;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    private String description;

    @Min(0)
    @Max(365)
    private Integer trialDays;
}
