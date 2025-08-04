package com.signature.autosave.modules.paymentmethod.dto;

import com.signature.autosave.modules.paymentmethod.domain.enums.PaymentMethodType;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class PixResponseDTO extends PaymentMethodResponseDTO {
    private String pixKey;

    public PixResponseDTO(UUID id, PaymentMethodType type, String pixKey, LocalDate createdAt) {
        super(id, type, createdAt);
        this.pixKey = pixKey;

    }
}
