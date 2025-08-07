package com.signature.autosave.modules.pay.payment.domain.entity;

import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.infra.components.converter.PaymentConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_response")
@Getter
@Setter
public class PaymentResponse extends PaymentEntity {
    @NotBlank
    @Lob
    @Convert(converter = PaymentConverter.class)
    private Payment paymentResponse;

}
