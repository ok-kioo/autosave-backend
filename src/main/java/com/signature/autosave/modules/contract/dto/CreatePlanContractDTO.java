package com.signature.autosave.modules.contract.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreatePlanContractDTO (@NotBlank
                                     UUID paymentMethod,

                                     int installments,

                                     @NotBlank
                                     UUID subscriptionPlan) {

}
