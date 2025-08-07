package com.signature.autosave.modules.pay.payment.domain.entity;

import com.signature.autosave.infra.components.converter.PaymentConverter;
import com.signature.autosave.infra.components.converter.SubscriptionConverter;
import com.signature.autosave.modules.pay.payment.dto.Subscription;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payment")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @ManyToOne
    @JoinColumn(name = "payment_method", referencedColumnName = "id")
    private PaymentMethod paymentMethod;

    @Lob
    @Convert(converter = SubscriptionConverter.class)
    private Subscription subscriptionResponse;

    @Lob
    @Convert(converter = PaymentConverter.class)
    private com.mercadopago.resources.payment.Payment mpPayment;

}
