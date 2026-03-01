package com.signature.autosave.modules.payment.method.builder;

import com.signature.autosave.modules.payment.method.domain.entity.PaymentMethod;
import com.signature.autosave.modules.payment.method.domain.entity.PixPaymentMethod;
import jakarta.validation.constraints.NotBlank;

public class PixPaymentMethodBuilder {
    private final PixPaymentMethod instance = new PixPaymentMethod();

    private PixPaymentMethodBuilder() {
    }

    public static PixPaymentMethodBuilder builder() {
        return new PixPaymentMethodBuilder();
    }

    public PixPaymentMethodBuilder withBase(@NotBlank PaymentMethod base) {
        instance.setType(base.getType());
        instance.setFirstName(base.getFirstName());
        instance.setLastName(base.getLastName());
        instance.setDocumentNumber(base.getDocumentNumber());
        instance.setCreatedAt(base.getCreatedAt());
        instance.setDefault(base.isDefault());
        instance.setUser(base.getUser());
        return this;
    }

    public PixPaymentMethodBuilder withCustomer(@NotBlank String customerId) {
        instance.setCustomerId(customerId);
        return this;
    }

    public PixPaymentMethod build() {
        if (instance.getType() == null) {
            throw new IllegalStateException("Método de pagamento não definido");
        }
        return instance;
    }
}
