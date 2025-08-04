package com.signature.autosave.modules.paymentmethod.dto;

import com.signature.autosave.modules.paymentmethod.domain.enums.PaymentMethodType;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class PaymentMethodResponseDTO {
    private final UUID id;
    private final PaymentMethodType type;
    private final LocalDate createdAt;

    public PaymentMethodResponseDTO(UUID id, PaymentMethodType type, LocalDate createdAt) {
        this.id = id;
        this.type = type;
        this.createdAt = createdAt;
    }
}
