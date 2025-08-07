package com.signature.autosave.modules.pay.payment.dto;

import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class PaymentEntityResponseDTO {
    @NotBlank
    private final UUID id;

    @NotBlank
    private final PaymentMethod paymentMethod;

    public PaymentEntityResponseDTO(UUID id, PaymentMethod paymentMethod) {
        this.id = id;
        this.paymentMethod = paymentMethod;
    }
}
