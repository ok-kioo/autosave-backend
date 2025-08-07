package com.signature.autosave.modules.pay.payment.builder;

import com.signature.autosave.modules.pay.payment.domain.entity.PaymentEntity;
import com.signature.autosave.modules.pay.payment.domain.entity.SubscriptionResponse;
import com.signature.autosave.modules.pay.payment.dto.Subscription;
import jakarta.validation.constraints.NotBlank;

public class SubscriptionResponseBuilder {
    private final SubscriptionResponse instance = new SubscriptionResponse();

    private SubscriptionResponseBuilder() {
    }

    public static SubscriptionResponseBuilder builder() {
        return new SubscriptionResponseBuilder();
    }

    public SubscriptionResponseBuilder withBase(PaymentEntity base) {
        instance.setPaymentMethod(base.getPaymentMethod());
        return this;
    }

    public SubscriptionResponseBuilder withSubscriptionResponse(@NotBlank Subscription subscriptionResponse) {
        instance.setSubscriptionResponse(subscriptionResponse);
        return this;
    }

    public SubscriptionResponse build() {
        return instance;
    }
}
