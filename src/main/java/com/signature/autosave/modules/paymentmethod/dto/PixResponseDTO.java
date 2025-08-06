package com.signature.autosave.modules.paymentmethod.dto;

import com.signature.autosave.modules.paymentmethod.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.user.domain.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PixResponseDTO extends PaymentMethodResponseDTO {
    private String pixKey;

    public PixResponseDTO(UUID id, PaymentMethodType type,
                          String firstName, String lastName, String email, String documentNumber,
                          LocalDateTime createdAt, boolean isDefault, User user) {

        super(id, type, firstName, lastName, email, documentNumber, createdAt, isDefault, user);

    }
}
