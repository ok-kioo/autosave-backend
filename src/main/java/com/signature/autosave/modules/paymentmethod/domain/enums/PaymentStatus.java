package com.signature.autosave.modules.paymentmethod.domain.enums;

public enum PaymentStatus {
     COMPLETED ("COMPLETED")
     ,PENDING ("PENDING")
     ,FAILED ("FAILED")
     ,REFUNDED ("REFUNDED")
     ,CANCELLED ("CANCELLED");

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }
}
