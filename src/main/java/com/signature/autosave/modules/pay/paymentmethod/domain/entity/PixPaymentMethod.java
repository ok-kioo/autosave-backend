package com.signature.autosave.modules.pay.paymentmethod.domain.entity;

import com.mercadopago.resources.customer.Customer;
import com.signature.autosave.infra.components.converter.SubscriptionConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pix_method")
@Getter
@Setter
public class PixPaymentMethod extends PaymentMethod {
    @Lob
    @Convert(converter = SubscriptionConverter.class)
    private Customer customer;
    private String pixKey;
}
