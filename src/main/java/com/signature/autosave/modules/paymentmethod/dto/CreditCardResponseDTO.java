package com.signature.autosave.modules.paymentmethod.dto;

import com.signature.autosave.modules.paymentmethod.domain.enums.PaymentMethodType;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class CreditCardResponseDTO extends PaymentMethodResponseDTO {
    private final String token;
    private final String cardHolderName;
    private final int lastFourDigits;
    private final boolean isDefault;

    public CreditCardResponseDTO(UUID id, PaymentMethodType type,
                                 String token, String cardHolderName, int lastFourDigits,
                                 boolean isDefault, LocalDate createdAt) {
        super(id, type, createdAt);
        this.token = token;
        this.cardHolderName = cardHolderName;
        this.lastFourDigits = lastFourDigits;
        this.isDefault = isDefault;
    }

}
