package com.signature.autosave.modules.pay.payment.dto;

import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class PaymentResponseDTO {
    @NotBlank
    private final UUID id;

    @NotBlank
    private final PaymentMethod paymentMethod;

    private final Payment monthlyPayment;

    private final Subscription subscription;

    public PaymentResponseDTO(UUID id, PaymentMethod paymentMethod, Payment payment, Subscription subscription) {
        this.id = id;
        this.paymentMethod = paymentMethod;
        this.monthlyPayment = payment;
        this.subscription = subscription;
    }
}
