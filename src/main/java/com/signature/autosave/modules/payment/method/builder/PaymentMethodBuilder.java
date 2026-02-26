package com.signature.autosave.modules.payment.method.builder;

import com.signature.autosave.modules.payment.method.domain.entity.PaymentMethod;
import com.signature.autosave.modules.payment.method.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class PaymentMethodBuilder {
    private PaymentMethodType type;
    private String firstName;
    private String lastName;
    private String email;
    private String documentNumber;
    private LocalDateTime createdAt;
    private boolean isDefault;
    private User user;

    private PaymentMethodBuilder() {
    }

    public static PaymentMethodBuilder builder() {
        return new PaymentMethodBuilder();
    }
    public PaymentMethodBuilder withType(@NotBlank PaymentMethodType type) {
        this.type = type;
        return this;
    }
    public PaymentMethodBuilder withFirstName(@NotBlank String firstName) {
        this.firstName = firstName;
        return this;
    }
    public PaymentMethodBuilder withLastName(@NotBlank String lastName) {
        this.lastName = lastName;
        return this;
    }
    public PaymentMethodBuilder withEmail(@NotBlank @Email String email) {
        this.email = email;
        return this;
    }
    public PaymentMethodBuilder withDocumentNumber(@NotBlank String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }
    public PaymentMethodBuilder withCreatedAt(@NotBlank LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    public PaymentMethodBuilder withIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }
    public PaymentMethodBuilder withUser(@NotBlank User user) {
        this.user = user;
        return this;
    }

    public PaymentMethod build() {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setType(type);
        paymentMethod.setFirstName(firstName);
        paymentMethod.setLastName(lastName);
        paymentMethod.setDocumentNumber(documentNumber);
        paymentMethod.setCreatedAt(createdAt);
        paymentMethod.setDefault(isDefault);
        paymentMethod.setUser(user);

        return paymentMethod;
    }
}