package com.signature.autosave.modules.paymentmethod.domain.entity;

import com.mercadopago.resources.customer.CustomerCard;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "credit_card_method")
@Getter
@Setter
public class CreditCardPaymentMethod extends PaymentMethod {
    @NotBlank
    private CustomerCard customerCard;
}
