package com.signature.autosave.infra.components.intermediation;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.customer.Customer;
import com.mercadopago.resources.customer.CustomerCard;
import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.modules.payment.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.payment.domain.entity.PixPaymentMethod;
import com.signature.autosave.modules.payment.dto.RegisterPaymentMethodDTO;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;

public interface IIntermediationComponent {
    Payment createPixPayment(PixPaymentMethod pixPaymentMethod, SubscriptionPlan subscriptionPlan, String idempotencyKey) throws MPException, MPApiException;
    Payment createCreditCardPayment(CreditCardPaymentMethod creditCardPaymentMethod, SubscriptionPlan subscriptionPlan, Integer installments, String idempotencyKey) throws MPException, MPApiException;

    String createPreapprovalPlan(SubscriptionPlan plan);
    String createSubscription(String mpPreapprovalPlanId, CreditCardPaymentMethod creditCardPaymentMethod, String idempotencyKey);

    Customer createCustomer(RegisterPaymentMethodDTO registerPaymentMethodDTO) throws MPException, MPApiException;
    CustomerCard saveCreditCard(RegisterPaymentMethodDTO registerPaymentMethodDTO, Customer customer) throws MPException, MPApiException;
}
