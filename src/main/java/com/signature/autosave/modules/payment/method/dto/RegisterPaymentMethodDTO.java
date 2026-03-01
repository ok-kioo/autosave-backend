package com.signature.autosave.modules.payment.method.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.signature.autosave.modules.payment.method.domain.enums.PaymentMethodType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegisterPaymentMethodDTO {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    private PaymentMethodType type;

    private String gatewayToken;

    private String gatewayIssuerId;

    private String gatewayPaymentMethodId;

    @JsonProperty("isDefault")
    private boolean isDefault;
}
