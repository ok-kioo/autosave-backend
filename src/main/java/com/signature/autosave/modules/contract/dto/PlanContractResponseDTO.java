package com.signature.autosave.modules.contract.dto;

import com.signature.autosave.modules.contract.domain.enums.BillingStatus;
import com.signature.autosave.modules.payment.domain.entity.PaymentMethod;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class PlanContractResponseDTO {
    @NotBlank
    private final UUID id;

    @NotNull
    private SubscriptionPlan subscriptionPlan;

    @NotBlank
    private final PaymentMethod paymentMethod;

    @NotNull
    private String contractId;

    @NotNull
    private BillingStatus status;

    @NotNull
    private Boolean isRecurring;

    @NotNull
    private LocalDateTime startedAt;

    @NotNull
    private LocalDateTime endsAt;

    public PlanContractResponseDTO(UUID id, SubscriptionPlan subscriptionPlan, PaymentMethod paymentMethod, String contractId, BillingStatus status, Boolean isRecurring, LocalDateTime startedAt, LocalDateTime endsAt) {
        this.id = id;
        this.subscriptionPlan = subscriptionPlan;
        this.paymentMethod = paymentMethod;
        this.contractId = contractId;
        this.status = status;
        this.isRecurring = isRecurring;
        this.startedAt = startedAt;
        this.endsAt = endsAt;
    }
}
