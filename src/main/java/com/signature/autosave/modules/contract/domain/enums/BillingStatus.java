package com.signature.autosave.modules.contract.domain.enums;

public enum BillingStatus {
    PENDING ("PENDING"),
    PAID ("PAID"),
    FAILED ("FAILED"),
    CANCELED ("CANCELED"),
    REFUNDED ("REFUNDED");

    private final String status;

    BillingStatus(String status) {
        this.status = status;
    }

}
