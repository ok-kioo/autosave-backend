package com.signature.autosave.modules.paymentmethod.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.signature.autosave.modules.paymentmethod.domain.enums.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegisterPaymentMethodDTO {
    @NotBlank
    private PaymentMethodType type;

    private String token;

    private String cardHolderName;

    private int lastFourDigits;

    private String pixKey;

    @JsonProperty("isDefault")
    private boolean isDefault;
}
