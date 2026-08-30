package com.signature.autosave.modules.subscription.dto;

import com.signature.autosave.modules.subscription.domain.enums.BillingCycle;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateSubscriptionPlanDTO(@NotNull
                                        String name,

                                        @NotNull
                                        BigDecimal price,

                                        @Enumerated(EnumType.STRING)
                                        BillingCycle billingCycle,

                                        String description,

                                        @NotNull
                                        @Min(0)
                                        @Max(365)
                                        Integer trialDays) {

}
