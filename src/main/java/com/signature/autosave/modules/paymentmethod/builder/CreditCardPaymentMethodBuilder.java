package com.signature.autosave.modules.paymentmethod.builder;

import com.signature.autosave.modules.paymentmethod.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;

public class CreditCardPaymentMethodBuilder {
    private final CreditCardPaymentMethod instance = new CreditCardPaymentMethod();

    public static CreditCardPaymentMethodBuilder builder() {
        return new CreditCardPaymentMethodBuilder();
    }

    public CreditCardPaymentMethodBuilder withBase(PaymentMethod base) {
        instance.setType(base.getType());
        instance.setCreatedAt(base.getCreatedAt());
        instance.setUser(base.getUser());
        return this;
    }

    public CreditCardPaymentMethodBuilder withToken(String token) {
        instance.setToken(token);
        return this;
    }
    public CreditCardPaymentMethodBuilder withCardHolderName(String cardHolderName) {
        instance.setCardHolderName(cardHolderName);
        return this;
    }
    public CreditCardPaymentMethodBuilder withLastFourDigits(int lastFourDigits) {
        instance.setLastFourDigits(lastFourDigits);
        return this;
    }
    public CreditCardPaymentMethodBuilder withIsDefault(@NotBlank boolean isDefault) {
        instance.setDefault(isDefault);
        return this;
    }

    public CreditCardPaymentMethod build() {
        if (instance.getType() == null) {
            throw new IllegalStateException("Método de pagamento não definido");
        }
        return instance;
    }
}
