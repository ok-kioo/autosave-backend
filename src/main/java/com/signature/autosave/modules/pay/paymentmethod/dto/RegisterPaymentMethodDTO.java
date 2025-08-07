package com.signature.autosave.modules.pay.paymentmethod.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.signature.autosave.modules.pay.paymentmethod.domain.enums.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegisterPaymentMethodDTO {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String email;

    @NotBlank
    private String documentNumber;

    @NotBlank
    private PaymentMethodType type;

    private String token;

    private String issuerId;

    private String paymentMethodId;

    @JsonProperty("isDefault")
    private boolean isDefault;
}
