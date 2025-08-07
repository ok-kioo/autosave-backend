package com.signature.autosave.modules.pay.paymentmethod.domain.enums;

import lombok.Getter;

@Getter
public enum PaymentMethodType {
    CREDIT_CARD("Credit Card"),
    PIX("Pix");

    private final String type;

    PaymentMethodType(String type) {
        this.type = type;
    }

}
