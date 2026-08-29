package com.signature.autosave.modules.payment.payload.dto;

import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import com.signature.autosave.modules.payment.payload.domain.enums.PayloadType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PayloadResponseDTO(@NotNull UUID id, @NotNull BigDecimal amount, @NotNull long paymentId,
                                 @NotNull PayloadType payloadType, @NotNull PlanContract planContract) {

}
