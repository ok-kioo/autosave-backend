package com.signature.autosave.modules.pay.payment.builder;

import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.modules.pay.payment.domain.entity.PaymentEntity;
import com.signature.autosave.modules.pay.payment.domain.entity.PaymentResponse;
import jakarta.validation.constraints.NotBlank;

public class PaymentResponseBuilder {
    private final PaymentResponse instance = new PaymentResponse();

    private PaymentResponseBuilder() {
    }

    public static PaymentResponseBuilder builder() {
        return new PaymentResponseBuilder();
    }

    public PaymentResponseBuilder withBase(PaymentEntity base) {
        instance.setPaymentMethod(base.getPaymentMethod());
        return this;
    }

    public PaymentResponseBuilder withPaymentResponse(@NotBlank Payment paymentResponse) {
        instance.setPaymentResponse(paymentResponse);
        return this;
    }

    public PaymentResponse build() {
        return instance;
    }
}
