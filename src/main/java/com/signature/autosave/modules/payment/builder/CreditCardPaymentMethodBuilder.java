package com.signature.autosave.modules.payment.builder;

import com.signature.autosave.modules.payment.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.payment.domain.entity.PaymentMethod;

public class CreditCardPaymentMethodBuilder {
    private final CreditCardPaymentMethod instance = new CreditCardPaymentMethod();

    private CreditCardPaymentMethodBuilder() {
    }

    public static CreditCardPaymentMethodBuilder builder() {
        return new CreditCardPaymentMethodBuilder();
    }

    public CreditCardPaymentMethodBuilder withBase(PaymentMethod base) {
        instance.setType(base.getType());
        instance.setFirstName(base.getFirstName());
        instance.setLastName(base.getLastName());
        instance.setDocumentNumber(base.getDocumentNumber());
        instance.setCreatedAt(base.getCreatedAt());
        instance.setDefault(base.isDefault());
        instance.setUser(base.getUser());
        return this;
    }

    public CreditCardPaymentMethodBuilder withCustomerCard(String customerCardId) {
        instance.setCustomerCardId(customerCardId);
        return this;
    }

    public CreditCardPaymentMethodBuilder withCustomer(String customerId) {
        instance.setCustomerId(customerId);
        return this;
    }

    public CreditCardPaymentMethod build() {
        if (instance.getType() == null) {
            throw new IllegalStateException("Método de pagamento não definido");
        }
        return instance;
    }
}
