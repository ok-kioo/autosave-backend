package com.signature.autosave.modules.contract.domain.entity;

import com.signature.autosave.modules.contract.domain.enums.BillingStatus;
import com.signature.autosave.modules.payment.method.domain.entity.PaymentMethod;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "plan_contract")
public class PlanContract {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "subscription_plan_id", referencedColumnName = "id")
    private SubscriptionPlan subscriptionPlan;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "payment_method_id", referencedColumnName = "id")
    private PaymentMethod paymentMethod;

    private String contractId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private BillingStatus status;

    @NotNull
    private Boolean isRecurring;

    @Column(updatable = false)
    private LocalDate startedAt;

    private LocalDate endsAt;
}
