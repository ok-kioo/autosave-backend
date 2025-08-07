package com.signature.autosave.modules.pay.paymentmethod.builder;

import com.mercadopago.resources.customer.Customer;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PaymentMethod;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PixPaymentMethod;
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
        instance.setEmail(base.getEmail());
        instance.setDocumentNumber(base.getDocumentNumber());
        instance.setCreatedAt(base.getCreatedAt());
        instance.setDefault(base.isDefault());
        instance.setUser(base.getUser());
        return this;
    }

    public PixPaymentMethodBuilder withPixKey(String pixKey) {
        instance.setPixKey(pixKey);
        return this;
    }

    public PixPaymentMethodBuilder withCustomer(@NotBlank Customer customer) {
        instance.setCustomer(customer);
        return this;
    }

    public PixPaymentMethod build() {
        if (instance.getType() == null) {
            throw new IllegalStateException("Método de pagamento não definido");
        }
        return instance;
    }
}
