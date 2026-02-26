package com.signature.autosave.modules.payment.method.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "credit_card_method")
@Getter
@Setter
public class CreditCardPaymentMethod extends PaymentMethod {
    @NotNull
    private String customerId;
    @NotNull
    private String customerCardId;
}
