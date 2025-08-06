package com.signature.autosave.infra.components.intermediation;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.cardtoken.CardTokenClient;
import com.mercadopago.client.cardtoken.CardTokenRequest;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.customer.CustomerCardClient;
import com.mercadopago.client.customer.CustomerCardCreateRequest;
import com.mercadopago.client.customer.CustomerClient;
import com.mercadopago.client.customer.CustomerRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.CardToken;
import com.mercadopago.resources.customer.Customer;
import com.mercadopago.resources.customer.CustomerCard;
import com.mercadopago.resources.customer.CustomerCardIssuer;
import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.modules.payment.dto.CreatePaymentDTO;
import com.signature.autosave.modules.paymentmethod.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.entity.PixPaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.repository.CreditCardPaymentMethodRepository;
import com.signature.autosave.modules.paymentmethod.domain.repository.PixPaymentMethodRepository;
import com.signature.autosave.modules.paymentmethod.dto.RegisterPaymentMethodDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MPComponent {
    private final CreditCardPaymentMethodRepository creditCardPaymentMethodRepository;
    private final PixPaymentMethodRepository pixPaymentMethodRepository;

    static {
        MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));
    }

    public void createPixPayment(CreatePaymentDTO createPaymentDTO, String idempotencyKey) throws MPException, MPApiException {
        PixPaymentMethod pix = pixPaymentMethodRepository.findById(createPaymentDTO.getPaymentMethod().getId())
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("x-idempotency-key", idempotencyKey);

        MPRequestOptions requestOptions = MPRequestOptions.builder()
                .customHeaders(customHeaders)
                .build();

        PaymentClient client = new PaymentClient();

        PaymentCreateRequest paymentCreateRequest =
                PaymentCreateRequest.builder()
                        .transactionAmount(new BigDecimal("100"))
                        .description("Autosave - Plano Mensal Basic")
                        .paymentMethodId("pix")
                        .dateOfExpiration(OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC))
                        .payer(
                                PaymentPayerRequest.builder()
                                        .firstName(pix.getFirstName())
                                        .lastName(pix.getLastName())
                                        .email(pix.getEmail())
                                        .identification(
                                                IdentificationRequest.builder().type("CPF").number(pix.getDocumentNumber()).build())
                                        .build())
                        .build();

        Payment result = client.create(paymentCreateRequest, requestOptions);
    }

    public void createCreditCardPayment(CreatePaymentDTO createPaymentDTO, String idempotencyKey) throws MPException, MPApiException {
        CreditCardPaymentMethod creditCard = creditCardPaymentMethodRepository
                .findById(createPaymentDTO.getPaymentMethod().getId())
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("x-idempotency-key", idempotencyKey);

        MPRequestOptions requestOptions = MPRequestOptions.builder()
                .customHeaders(customHeaders)
                .build();

        PaymentClient client = new PaymentClient();

        PaymentCreateRequest paymentCreateRequest =
                PaymentCreateRequest.builder()
                        .transactionAmount(new BigDecimal("100"))
                        .token(createCardToken(creditCard.getCustomerCard(), createPaymentDTO).getId())
                        .description("Autosave - Plano Mensal Basic")
                        .installments(createPaymentDTO.getInstallments())
                        .paymentMethodId(creditCard.getCustomerCard().getPaymentMethod().getId())
                        .payer(
                                PaymentPayerRequest.builder()
                                        .firstName(creditCard.getFirstName())
                                        .lastName(creditCard.getLastName())
                                        .email(creditCard.getEmail())
                                        .identification(
                                                IdentificationRequest.builder()
                                                        .type("CPF")
                                                        .number(creditCard.getDocumentNumber())
                                                        .build())
                                        .build())
                        .build();

        Payment result = client.create(paymentCreateRequest, requestOptions);
    }

    public CustomerCard savePaymentMethod(RegisterPaymentMethodDTO registerPaymentMethodDTO) throws MPException, MPApiException {
        CustomerRequest customerRequest = CustomerRequest.builder()
                .firstName(registerPaymentMethodDTO.getFirstName())
                .lastName(registerPaymentMethodDTO.getLastName())
                .email(registerPaymentMethodDTO.getEmail())
                .build();
        Customer customer = new CustomerClient().create(customerRequest);

        CustomerCardIssuer issuer = CustomerCardIssuer.builder()
                .id(registerPaymentMethodDTO.getIssuerId())
                .build();

        CustomerCardCreateRequest cardCreateRequest = CustomerCardCreateRequest.builder()
                .token(registerPaymentMethodDTO.getToken())
                .issuer(issuer)
                .paymentMethodId(registerPaymentMethodDTO.getPaymentMethodId())
                .build();

        return new CustomerCardClient().create(customer.getId(), cardCreateRequest);
    }

    private CardToken createCardToken(CustomerCard customerCard, CreatePaymentDTO createPaymentDTO) throws MPException, MPApiException {
        CardTokenRequest cardTokenRequest = CardTokenRequest.builder()
                .cardId(customerCard.getId())
                .customerId(customerCard.getCustomerId())
                .securityCode(String.valueOf(createPaymentDTO.getSecurityCode()))
                .build();

        return new CardTokenClient().create(cardTokenRequest);
    }

}
