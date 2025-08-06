package com.signature.autosave.modules.paymentmethod.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdatePaymentMethodDTO {
    @NotBlank
    @JsonProperty("isDefault")
    private boolean isDefault;
}
