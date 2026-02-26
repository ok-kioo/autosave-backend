package com.signature.autosave.modules.contract.builder;

import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import com.signature.autosave.modules.contract.domain.enums.BillingStatus;
import com.signature.autosave.modules.payment.method.domain.entity.PaymentMethod;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class PlanContractBuilder {
    private SubscriptionPlan subscriptionPlan;
    private PaymentMethod paymentMethod;
    private String contractId;
    private BillingStatus status;
    private Boolean isRecurring;
    private LocalDate startedAt;
    private LocalDate endsAt;

    private PlanContractBuilder() {
    }

    public static PlanContractBuilder builder() {
        return new PlanContractBuilder();
    }

    public PlanContractBuilder withPaymentMethod(@NotNull PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public PlanContractBuilder withSubscriptionPlan(@NotNull SubscriptionPlan subscriptionPlan){
        this.subscriptionPlan = subscriptionPlan;
        return this;
    }

    public PlanContractBuilder withContractId(@NotNull String contractId){
        this.contractId = contractId;
        return this;
    }

    public PlanContractBuilder withStatus(@NotNull BillingStatus status){
        this.status = status;
        return this;
    }

    public PlanContractBuilder withIsRecurring(@NotNull Boolean isRecurring){
        this.isRecurring = isRecurring;
        return this;
    }

    public PlanContractBuilder withStartedAt(@NotNull LocalDate startedAt){
        this.startedAt = startedAt;
        return this;
    }

    public PlanContractBuilder withEndsAt(LocalDate endsAt){
        this.endsAt = endsAt;
        return this;
    }

    public PlanContract build() {
        PlanContract planContract = new PlanContract();
        planContract.setPaymentMethod(paymentMethod);
        planContract.setSubscriptionPlan(subscriptionPlan);
        planContract.setContractId(contractId);
        planContract.setStatus(status);
        planContract.setIsRecurring(isRecurring);
        planContract.setStartedAt(startedAt);
        planContract.setEndsAt(endsAt);

        return planContract;
    }
}
