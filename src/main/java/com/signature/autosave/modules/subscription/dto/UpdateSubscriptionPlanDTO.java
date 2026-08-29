package com.signature.autosave.modules.subscription.dto;

import com.signature.autosave.modules.subscription.domain.enums.BillingCycle;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdateSubscriptionPlanDTO(String name,

                                        BigDecimal price,

                                        @Enumerated(EnumType.STRING)
                                        BillingCycle billingCycle,

                                        String description,

                                        @Min(0)
                                        @Max(365)
                                        Integer trialDays) {

}
