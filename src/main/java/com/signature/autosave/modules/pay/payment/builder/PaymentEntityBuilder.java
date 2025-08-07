package com.signature.autosave.modules.pay.payment.builder;

import com.signature.autosave.modules.pay.payment.domain.entity.PaymentEntity;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;

public class PaymentEntityBuilder {
    private PaymentMethod paymentMethod;

    private PaymentEntityBuilder() {
    }

    public static PaymentEntityBuilder builder() {
        return new PaymentEntityBuilder();
    }
    public PaymentEntityBuilder withPaymentMethod(@NotBlank PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public PaymentEntity build() {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPaymentMethod(paymentMethod);

        return paymentEntity;
    }
}
