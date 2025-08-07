package com.signature.autosave.modules.pay.paymentmethod.domain.entity;

import com.mercadopago.resources.customer.CustomerCard;
import com.signature.autosave.infra.components.converter.CustomerCardConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "credit_card_method")
@Getter
@Setter
public class CreditCardPaymentMethod extends PaymentMethod {
    @Lob
    @Convert(converter = CustomerCardConverter.class)
    private CustomerCard customerCard;
}
