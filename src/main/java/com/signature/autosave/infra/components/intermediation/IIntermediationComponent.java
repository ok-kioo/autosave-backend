package com.signature.autosave.infra.components.intermediation;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.customer.Customer;
import com.mercadopago.resources.customer.CustomerCard;
import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.modules.pay.payment.dto.CreatePaymentDTO;
import com.signature.autosave.modules.pay.payment.dto.Subscription;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PaymentMethod;
import com.signature.autosave.modules.pay.paymentmethod.dto.RegisterPaymentMethodDTO;

public interface IIntermediationComponent {
    Payment createPixPayment(CreatePaymentDTO createPaymentDTO, String idempotencyKey) throws MPException, MPApiException;
    Payment createCreditCardPayment(CreatePaymentDTO createPaymentDTO, String idempotencyKey) throws MPException, MPApiException;

    Subscription createSubscriptionPlan(String paymentType, String paymentMethod, PaymentMethod paymentMethodEntity, CreatePaymentDTO createPaymentDTO, String idempotencyKey) throws MPException, MPApiException;

    Customer createCustomer(RegisterPaymentMethodDTO registerPaymentMethodDTO) throws MPException, MPApiException;
    CustomerCard saveCreditCard(RegisterPaymentMethodDTO registerPaymentMethodDTO, Customer customer) throws MPException, MPApiException;
}
