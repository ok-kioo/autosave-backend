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
import com.signature.autosave.modules.pay.payment.dto.CreatePaymentDTO;
import com.signature.autosave.modules.pay.payment.dto.Subscription;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PaymentMethod;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PixPaymentMethod;
import com.signature.autosave.modules.pay.paymentmethod.domain.repository.CreditCardPaymentMethodRepository;
import com.signature.autosave.modules.pay.paymentmethod.domain.repository.PixPaymentMethodRepository;
import com.signature.autosave.modules.pay.paymentmethod.dto.RegisterPaymentMethodDTO;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.function.IOLongSupplier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.function.Uncheck.getAsLong;

@Component
@RequiredArgsConstructor
public class MPComponent {
    private final CreditCardPaymentMethodRepository creditCardPaymentMethodRepository;
    private final PixPaymentMethodRepository pixPaymentMethodRepository;

    static {
        MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));
    }

    public Payment createPixPayment(CreatePaymentDTO createPaymentDTO, String idempotencyKey) throws MPException, MPApiException {
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
                        .description("Autosave - Plano Anual Premium")
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

        return client.create(paymentCreateRequest, requestOptions);
    }

    public Payment createCreditCardPayment(CreatePaymentDTO createPaymentDTO, String idempotencyKey) throws MPException, MPApiException {
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
                        .transactionAmount(new BigDecimal("150"))
                        .token(createCardToken(creditCard.getCustomerCard(), createPaymentDTO).getId())
                        .description("Autosave - Plano Anual Premium")
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

        return client.create(paymentCreateRequest, requestOptions);
    }

    public Subscription createSubscriptionPlan(String paymentType, String paymentMethod, PaymentMethod paymentMethodEntity, CreatePaymentDTO createPaymentDTO, String idempotencyKey) throws MPException, MPApiException {
        HttpResponse<JsonNode> planResponse = Unirest.post("https://api.mercadopago.com/preapproval_plan")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + System.getenv("MP_ACCESS_TOKEN"))
                .header("x-idempotency-key", idempotencyKey)
                .body(Map.of(
                        "reason", "Autosave - Plano Mensal Basic",
                        "back_url", "https://www.youtube.com",

                        // Auto Recurring
                        "auto_recurring", Map.of(
                                "frequency", 1,
                                "frequency_type", "months",
                                "billing_day", 10,
                                "billing_day_proportional", true,
                                "transaction_amount", 100.00,
                                "currency_id", "BRL"

                                        /* Free Trial
                                        "free_trial", Map.of(
                                                "frequency", 1,
                                                "frequency_type", "months"
                                        )*/
                        ),

                        // Payment methods allowed
                        "payment_methods_allowed", Map.of(
                                "payment_types", List.of(
                                        Map.of("id", paymentType)
                                ),
                                "payment_methods", List.of(
                                        Map.of("id", paymentMethod)
                                )
                        )

                )).asJson();

        if (planResponse.getStatus() != 201) {
            throw new RuntimeException("Failed to create subscription plan: " + planResponse.getBody());
        }

        Map<String, Object> responseMap = planResponse.getBody().getObject().toMap();

        return createSubscription(paymentMethodEntity, responseMap, createPaymentDTO, idempotencyKey);
    }

    private Subscription createSubscription(PaymentMethod paymentMethod, Map<String, Object> planResponseMap, CreatePaymentDTO createPaymentDTO, String idempotencyKey) throws MPException, MPApiException {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> autoRecurring = (Map<String, Object>) planResponseMap.get("auto_recurring");

        requestBody.put("preapproval_plan_id", planResponseMap.get("id"));
        requestBody.put("reason", planResponseMap.get("reason"));
        requestBody.put("payer_email", paymentMethod.getEmail());
        requestBody.put("back_url", "https://www.youtube.com");
        requestBody.put("status", "pending");
        requestBody.put("auto_recurring", autoRecurring);

        if (paymentMethod instanceof CreditCardPaymentMethod creditCardPaymentMethod) {
            requestBody.put("card_token_id", createCardToken(creditCardPaymentMethod.getCustomerCard(), createPaymentDTO).getId());
        }

        HttpResponse<JsonNode> subscriptionResponse = Unirest.post("https://api.mercadopago.com/preapproval")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + System.getenv("MP_ACCESS_TOKEN"))
                .header("x-idempotency-key", idempotencyKey)
                .body(requestBody)
                .asJson();

        if (subscriptionResponse.getStatus() != 201) {
            throw new RuntimeException("Failed to create subscription: " + subscriptionResponse.getBody());
        }

        Map<String, Object> responseMap = subscriptionResponse.getBody().getObject().toMap();

        return buildSubscriptionDTO(responseMap);
    }

    private CardToken createCardToken(CustomerCard customerCard, CreatePaymentDTO createPaymentDTO) throws MPException, MPApiException {
        CardTokenRequest cardTokenRequest = CardTokenRequest.builder()
                .cardId(customerCard.getId())
                .customerId(customerCard.getCustomerId())
                .securityCode(createPaymentDTO.getSecurityCode())
                .build();

        return new CardTokenClient().create(cardTokenRequest);
    }

    public Customer createCustomer(RegisterPaymentMethodDTO registerPaymentMethodDTO) throws MPException, MPApiException {
        CustomerRequest customerRequest = CustomerRequest.builder()
                .firstName(registerPaymentMethodDTO.getFirstName())
                .lastName(registerPaymentMethodDTO.getLastName())
                .email(registerPaymentMethodDTO.getEmail())
                .build();

        return new CustomerClient().create(customerRequest);
    }

    public CustomerCard saveCreditCard(RegisterPaymentMethodDTO registerPaymentMethodDTO, Customer customer) throws MPException, MPApiException {
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

    private Subscription buildSubscriptionDTO(Map<String, Object> responseMap) {
        Subscription dto = new Subscription();

        dto.setId((String) responseMap.get("id"));
        dto.setVersion((Integer) responseMap.get("version"));
        dto.setApplicationId(getAsLong((IOLongSupplier) responseMap.get("application_id")));
        dto.setCollectorId(getAsLong((IOLongSupplier) responseMap.get("collector_id")));
        dto.setPreapprovalPlanId((String) responseMap.get("preapproval_plan_id"));
        dto.setReason((String) responseMap.get("reason"));
        dto.setExternalReference((String) responseMap.get("external_reference"));
        dto.setBackUrl((String) responseMap.get("back_url"));
        dto.setInitPoint((String) responseMap.get("init_point"));
        dto.setPayerId(getAsLong((IOLongSupplier) responseMap.get("payer_id")));

        if (responseMap.get("auto_recurring") != null) {
            dto.setAutoRecurring(buildAutoRecurring((Map<String, Object>) responseMap.get("auto_recurring")));
        }

        if(responseMap.get("card_id") != null) {
            dto.setCardId((String) responseMap.get("card_id"));
        }

        dto.setPaymentMethodId((String) responseMap.get("payment_method_id"));
        dto.setNextPaymentDate((Date) responseMap.get("next_payment_date"));
        dto.setDateCreated((Date) responseMap.get("date_created"));
        dto.setLastModified((Date) responseMap.get("last_modified"));
        dto.setStatus((String) responseMap.get("status"));

        return dto;
    }

    private Subscription.AutoRecurring buildAutoRecurring(Map<String, Object> autoRecurringMap) {
        Subscription.AutoRecurring ar = new Subscription.AutoRecurring();

        ar.setFrequency((Integer) autoRecurringMap.get("frequency"));
        ar.setFrequencyType((String) autoRecurringMap.get("frequency_type"));
        ar.setTransaction_amount(((Number) autoRecurringMap.get("transaction_amount")).doubleValue());
        ar.setCurrencyId((String) autoRecurringMap.get("currency_id"));
        ar.setStartDate((Date)autoRecurringMap.get("start_date"));
        ar.setEndDate((Date) autoRecurringMap.get("end_date"));

        return ar;
    }

}
