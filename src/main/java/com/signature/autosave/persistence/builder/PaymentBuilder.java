package com.signature.autosave.persistence.builder;

import com.signature.autosave.persistence.entity.Payment;
import com.signature.autosave.persistence.entity.User;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class PaymentBuilder {
    private String method;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime expiresAt;
    private User userId;

    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    public PaymentBuilder withPaymentMethod(@NotBlank String method) {
        this.method = method;
        return this;
    }

    public PaymentBuilder withStatus(@NotBlank String status) {
        this.status = status;
        return this;
    }

    public PaymentBuilder withPaymentMade(@NotBlank LocalDateTime paidAt) {
        this.paidAt = paidAt;
        return this;
    }

    public PaymentBuilder withPaymentExpires(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public PaymentBuilder withPaying(@NotBlank User userId) {
        this.userId = userId;
        return this;
    }

    public Payment build() {
        Payment payment = new Payment();
        payment.setPaymentMethod(this.method);
        payment.setPaymentStatus(this.status);
        payment.setPaidAt(this.paidAt);
        payment.setExpiresAt(this.expiresAt);
        payment.setUserId(this.userId);
        return payment;
    }
}
