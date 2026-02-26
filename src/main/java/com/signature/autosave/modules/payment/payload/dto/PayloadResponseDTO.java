package com.signature.autosave.modules.payment.payload.dto;

import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import com.signature.autosave.modules.payment.payload.domain.enums.PayloadType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class PayloadResponseDTO {
    @NotNull
    private UUID id;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private long paymentId;

    @NotNull
    private PayloadType payloadType;

    @NotNull
    private PlanContract planContract;

    public PayloadResponseDTO(UUID id, BigDecimal amount, long paymentId, PayloadType payloadType, PlanContract planContract) {
        this.id = id;
        this.amount = amount;
        this.paymentId = paymentId;
        this.payloadType = payloadType;
        this.planContract = planContract;
    }
}
