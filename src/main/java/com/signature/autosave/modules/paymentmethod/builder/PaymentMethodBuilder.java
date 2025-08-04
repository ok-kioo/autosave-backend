package com.signature.autosave.modules.paymentmethod.builder;

import com.signature.autosave.modules.paymentmethod.domain.entity.PaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class PaymentMethodBuilder {
    private PaymentMethodType type;
    private LocalDate createdAt;
    private User user;

    public static PaymentMethodBuilder builder() {
        return new PaymentMethodBuilder();
    }
    public PaymentMethodBuilder withType(@NotBlank PaymentMethodType type) {
        this.type = type;
        return this;
    }
    public PaymentMethodBuilder withCreatedAt(@NotBlank LocalDate createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    public PaymentMethodBuilder withUser(@NotBlank User user) {
        this.user = user;
        return this;
    }

    public PaymentMethod build() {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setType(type);
        paymentMethod.setCreatedAt(createdAt);
        paymentMethod.setUser(user);

        return paymentMethod;
    }
}