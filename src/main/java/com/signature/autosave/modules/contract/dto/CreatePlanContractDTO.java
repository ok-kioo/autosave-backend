package com.signature.autosave.modules.contract.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreatePlanContractDTO {
    @NotBlank
    private UUID paymentMethod;

    private int installments;

    @NotBlank
    private UUID subscriptionPlan;
}
