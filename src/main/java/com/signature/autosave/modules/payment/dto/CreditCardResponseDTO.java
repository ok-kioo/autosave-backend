package com.signature.autosave.modules.payment.dto;

import com.signature.autosave.modules.payment.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.user.domain.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class CreditCardResponseDTO extends PaymentMethodResponseDTO {
    private final String cardHolderName;
    private final String cardIssuerName;
    private final String lastFourDigits;

    public CreditCardResponseDTO(UUID id, PaymentMethodType type,
                                 String firstName, String lastName, String documentNumber,
                                 LocalDateTime createdAt, boolean isDefault, User user,
                                 String cardHolderName, String cardIssuerName, String lastFourDigits) {

        super(id, type, firstName, lastName, documentNumber, createdAt, isDefault, user);
        this.cardHolderName = cardHolderName;
        this.cardIssuerName = cardIssuerName;
        this.lastFourDigits = lastFourDigits;
    }

}
