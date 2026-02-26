package com.signature.autosave.modules.payment.dto;

import com.signature.autosave.modules.payment.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.user.domain.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PixResponseDTO extends PaymentMethodResponseDTO {

    public PixResponseDTO(UUID id, PaymentMethodType type,
                          String firstName, String lastName, String documentNumber,
                          LocalDateTime createdAt, boolean isDefault, User user) {

        super(id, type, firstName, lastName, documentNumber, createdAt, isDefault, user);
    }
}
