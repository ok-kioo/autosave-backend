package com.signature.autosave.modules.pay.paymentmethod.builder;

import com.mercadopago.resources.customer.CustomerCard;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PaymentMethod;

public class CreditCardPaymentMethodBuilder {
    private final CreditCardPaymentMethod instance = new CreditCardPaymentMethod();

    public static CreditCardPaymentMethodBuilder builder() {
        return new CreditCardPaymentMethodBuilder();
    }

    public CreditCardPaymentMethodBuilder withBase(PaymentMethod base) {
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

    public CreditCardPaymentMethodBuilder withCustomerCard(CustomerCard customerCard) {
        instance.setCustomerCard(customerCard);
        return this;
    }

    public CreditCardPaymentMethod build() {
        if (instance.getType() == null) {
            throw new IllegalStateException("Método de pagamento não definido");
        }
        return instance;
    }
}
