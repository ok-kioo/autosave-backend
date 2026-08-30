package com.signature.autosave.modules.subscription.dto;

import com.signature.autosave.modules.subscription.domain.enums.BillingCycle;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionPlanResponseDTO(@NotNull UUID id, @NotNull String name, @NotNull BigDecimal price,
                                          @NotNull BillingCycle billingCycle, @NotNull Integer trialDays,
                                          @NotNull LocalDateTime createdAt) {

}
