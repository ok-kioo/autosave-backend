package com.signature.autosave.modules.payment.dto;

import com.signature.autosave.modules.payment.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.user.domain.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PaymentMethodResponseDTO {
    private final UUID id;
    private final PaymentMethodType type;
    private final String firstName;
    private final String lastName;
    private final String documentNumber;
    private final LocalDateTime createdAt;
    private final boolean isDefault;
    private final User user;

    public PaymentMethodResponseDTO(UUID id, PaymentMethodType type, String firstName, String lastName, String documentNumber, LocalDateTime createdAt,
                                    boolean isDefault, User user) {
        this.id = id;
        this.type = type;
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentNumber = documentNumber;
        this.createdAt = createdAt;
        this.isDefault = isDefault;
        this.user = user;
    }
}
