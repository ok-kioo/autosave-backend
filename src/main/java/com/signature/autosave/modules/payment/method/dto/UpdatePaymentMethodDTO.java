package com.signature.autosave.modules.payment.method.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdatePaymentMethodDTO {
    @NotNull
    @JsonProperty("isDefault")
    private boolean isDefault;
}
