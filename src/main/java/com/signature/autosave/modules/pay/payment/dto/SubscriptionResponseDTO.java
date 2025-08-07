package com.signature.autosave.modules.pay.payment.dto;

import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PaymentMethod;

import java.util.UUID;

public class SubscriptionResponseDTO extends PaymentEntityResponseDTO {

    private final Subscription subscription;

    public SubscriptionResponseDTO(UUID id, PaymentMethod paymentMethod, Subscription subscription) {
        super(id, paymentMethod);
        this.subscription = subscription;
    }
}
