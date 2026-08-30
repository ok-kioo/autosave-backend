package com.signature.autosave.modules.contract.dto;

import com.signature.autosave.modules.contract.domain.enums.BillingStatus;
import com.signature.autosave.modules.payment.method.domain.entity.PaymentMethod;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record PlanContractResponseDTO (@NotBlank
                                       UUID id,

                                       @NotNull
                                       SubscriptionPlan subscriptionPlan,

                                       @NotBlank
                                       PaymentMethod paymentMethod,

                                       @NotNull
                                       String contractId,

                                       @NotNull
                                       BillingStatus status,

                                       @NotNull
                                       Boolean isRecurring,

                                       @NotNull
                                       LocalDate startedAt,

                                       @NotNull
                                       LocalDate endsAt) {

}
