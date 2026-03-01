package com.signature.autosave.modules.payment.method.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pix_method")
@Getter
@Setter
public class PixPaymentMethod extends PaymentMethod {
    private String customerId;
}
