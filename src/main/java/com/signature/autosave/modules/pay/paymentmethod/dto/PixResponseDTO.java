package com.signature.autosave.modules.pay.paymentmethod.dto;

import com.mercadopago.resources.customer.Customer;
import com.signature.autosave.modules.pay.paymentmethod.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.user.domain.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PixResponseDTO extends PaymentMethodResponseDTO {
    private final String pixKey;
    private final Customer customer;

    public PixResponseDTO(UUID id, PaymentMethodType type,
                          String firstName, String lastName, String email, String documentNumber,
                          LocalDateTime createdAt, boolean isDefault, User user, Customer customer) {

        super(id, type, firstName, lastName, email, documentNumber, createdAt, isDefault, user);
        this.pixKey = null;
        this.customer = customer;
    }
}
