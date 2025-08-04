package com.signature.autosave.modules.paymentmethod.builder;

import com.signature.autosave.modules.paymentmethod.domain.entity.PaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.entity.PixPaymentMethod;
import jakarta.validation.constraints.NotBlank;

public class PixPaymentMethodBuilder {
    private final PixPaymentMethod instance = new PixPaymentMethod();

    public static PixPaymentMethodBuilder builder() {
        return new PixPaymentMethodBuilder();
    }

    public PixPaymentMethodBuilder withBase(@NotBlank PaymentMethod base) {
        instance.setType(base.getType());
        instance.setCreatedAt(base.getCreatedAt());
        instance.setUser(base.getUser());
        return this;
    }

    public PixPaymentMethodBuilder withPixKey(String pixKey) {
        instance.setPixKey(pixKey);
        return this;
    }

    public PixPaymentMethod build() {
        if (instance.getType() == null) {
            throw new IllegalStateException("Método de pagamento não definido");
        }
        return instance;
    }
}
