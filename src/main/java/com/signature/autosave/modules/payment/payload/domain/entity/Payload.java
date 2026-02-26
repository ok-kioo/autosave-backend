package com.signature.autosave.modules.payment.payload.domain.entity;

import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import com.signature.autosave.modules.payment.payload.domain.enums.PayloadType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Table(name = "payload")
public class Payload {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    private UUID id;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private long paymentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PayloadType payloadType;

    @ManyToOne
    @JoinColumn(name = "plan_contract_id", referencedColumnName = "id")
    private PlanContract planContract;
}
