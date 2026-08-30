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
@Entity
@Table(name = "payment_payload")
public class Payload {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    private long paymentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PayloadType type;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "plan_contract_id", referencedColumnName = "id")
    private PlanContract planContract;
}
