package com.signature.autosave.infra.components.intermediation;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.customer.Customer;
import com.mercadopago.resources.customer.CustomerCard;
import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import com.signature.autosave.modules.payment.method.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.payment.method.domain.entity.PixPaymentMethod;
import com.signature.autosave.modules.payment.method.dto.RegisterPaymentMethodDTO;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;

import java.time.LocalDate;

public interface IIntermediationComponent {
    Payment createPixPayment(PixPaymentMethod pixPaymentMethod, SubscriptionPlan subscriptionPlan, PlanContract planContract, String idempotencyKey) throws MPApiException, MPException;
    Payment createCreditCardPayment(CreditCardPaymentMethod creditCardPaymentMethod, SubscriptionPlan subscriptionPlan, PlanContract planContract, Integer installments, String idempotencyKey) throws MPException, MPApiException;

    String createPreapprovalPlan(SubscriptionPlan plan);
    String createSubscription(String mpPreapprovalPlanId, CreditCardPaymentMethod creditCardPaymentMethod, PlanContract planContract, String idempotencyKey);
    void cancelSubscription(String preapprovalId);
    void refundPayment(Long paymentId) throws MPException, MPApiException;

    LocalDate getNextPaymentDate(String preapprovalId);

    Customer createCustomer(RegisterPaymentMethodDTO registerPaymentMethodDTO) throws MPException, MPApiException;
    CustomerCard saveCreditCard(RegisterPaymentMethodDTO registerPaymentMethodDTO, Customer customer) throws MPException, MPApiException;
}
