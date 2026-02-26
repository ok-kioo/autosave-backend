package com.signature.autosave.modules.payment.method.domain.enums;

import lombok.Getter;

@Getter
public enum PaymentMethodType {
    CREDIT_CARD("credit_card"),
    PIX("pix");

    private final String name;

    PaymentMethodType(String name) {
        this.name = name;
    }

}
