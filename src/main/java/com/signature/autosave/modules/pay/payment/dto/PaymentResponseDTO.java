package com.signature.autosave.modules.pay.payment.dto;

import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PaymentMethod;

import java.util.UUID;

public class PaymentResponseDTO extends PaymentEntityResponseDTO {

    private final Payment monthlyPayment;

    public PaymentResponseDTO(UUID id, PaymentMethod paymentMethod, Payment payment) {
        super(id, paymentMethod);
        this.monthlyPayment = payment;
    }
}
