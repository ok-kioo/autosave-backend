package com.signature.autosave.infra.components.intermediation;

import com.mercadopago.MercadoPagoConfig;
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
import com.mercadopago.net.MPResultsResourcesPage;
import com.mercadopago.net.MPSearchRequest;
import com.mercadopago.resources.customer.Customer;
import com.mercadopago.resources.customer.CustomerCard;
import com.mercadopago.resources.customer.CustomerCardIssuer;
import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.modules.payment.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.payment.domain.entity.PixPaymentMethod;
import com.signature.autosave.modules.payment.dto.RegisterPaymentMethodDTO;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MPComponent implements IIntermediationComponent{
    static {
        MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));
    }

    @Override
    public Payment createPixPayment(PixPaymentMethod pixPaymentMethod, SubscriptionPlan subscriptionPlan, String idempotencyKey) throws MPException, MPApiException {
        try {
            Map<String, String> customHeaders = new HashMap<>();
            customHeaders.put("x-idempotency-key", idempotencyKey);

            MPRequestOptions requestOptions = MPRequestOptions.builder()
                    .customHeaders(customHeaders)
                    .build();

            PaymentClient client = new PaymentClient();

            PaymentCreateRequest paymentCreateRequest =
                    PaymentCreateRequest.builder()
                            .transactionAmount(subscriptionPlan.getPrice())
                            .description(subscriptionPlan.getName())
                            .paymentMethodId("pix")
                            .dateOfExpiration(OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC))
                            .payer(
                                    PaymentPayerRequest.builder()
                                            .firstName(pixPaymentMethod.getFirstName())
                                            .lastName(pixPaymentMethod.getLastName())
                                            .email(pixPaymentMethod.getUser().getEmail())
                                            .identification(
                                                    IdentificationRequest.builder().type("CPF").number(pixPaymentMethod.getDocumentNumber()).build())
                                            .build())
                            .build();

            return client.create(paymentCreateRequest, requestOptions);
        } catch (MPApiException e) {
            System.out.println("MPApiException: " + e.getApiResponse().getContent());
            throw e;
        } catch (MPException e) {
            System.out.println("MPException: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Payment createCreditCardPayment(CreditCardPaymentMethod creditCardPaymentMethod, SubscriptionPlan subscriptionPlan, Integer installments, String idempotencyKey) throws MPException, MPApiException {
        try {
            Map<String, String> customHeaders = new HashMap<>();
            customHeaders.put("x-idempotency-key", idempotencyKey);

            MPRequestOptions requestOptions = MPRequestOptions.builder()
                    .customHeaders(customHeaders)
                    .build();

            CustomerCardClient customerCardClient = new CustomerCardClient();
            CustomerCard card = customerCardClient.get(creditCardPaymentMethod.getCustomerId(), creditCardPaymentMethod.getCustomerCardId());

            PaymentClient paymentClient = new PaymentClient();
            PaymentCreateRequest paymentCreateRequest =
                    PaymentCreateRequest.builder()
                            .transactionAmount(subscriptionPlan.getPrice())
                            .description(subscriptionPlan.getName())
                            .installments(installments)
                            .paymentMethodId(card.getPaymentMethod().getId())
                            .payer(
                                    PaymentPayerRequest.builder()
                                            .firstName(creditCardPaymentMethod.getFirstName())
                                            .lastName(creditCardPaymentMethod.getLastName())
                                            .email(creditCardPaymentMethod.getUser().getEmail())
                                            .identification(
                                                    IdentificationRequest.builder()
                                                            .type("CPF")
                                                            .number(creditCardPaymentMethod.getDocumentNumber())
                                                            .build())
                                            .build())
                            .build();

            return paymentClient.create(paymentCreateRequest, requestOptions);
        } catch (MPApiException e) {
            System.out.println("MPApiException: " + e.getApiResponse().getContent());
            throw e;
        } catch (MPException e) {
            System.out.println("MPException: " + e.getMessage());
            throw e;
        }
    }

    public String createPreapprovalPlan(SubscriptionPlan plan) {
        HttpResponse<JsonNode> response = Unirest.post(
                        "https://api.mercadopago.com/preapproval_plan")
                .header("Authorization", "Bearer " + System.getenv("MP_ACCESS_TOKEN"))
                .body(Map.of(
                        "reason", plan.getName(),
                        "auto_recurring", Map.of(
                                "frequency", 1,
                                "frequency_type", plan.getBillingCycle().getCycle(),
                                "transaction_amount", plan.getPrice(),
                                "currency_id", "BRL"
                        )
                )).asJson();

        if (response.getStatus() != 201) {
            throw new RuntimeException("Erro ao criar plano");
        }

        return response.getBody().getObject().getString("id");
    }

    public String createSubscription(String mpPreapprovalPlanId, CreditCardPaymentMethod creditCardPaymentMethod, String idempotencyKey) {
        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("preapproval_plan_id", mpPreapprovalPlanId);
        requestBody.put("payer_email", creditCardPaymentMethod.getUser().getEmail());
        requestBody.put("card_id", creditCardPaymentMethod.getCustomerCardId());
        requestBody.put("status", "authorized");

        HttpResponse<JsonNode> response = Unirest.post(
                        "https://api.mercadopago.com/preapproval")
                .header("Authorization", "Bearer " + System.getenv("MP_ACCESS_TOKEN"))
                .header("x-idempotency-key", idempotencyKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .asJson();

        if (response.getStatus() != 201) {
            throw new RuntimeException(
                    "Failed to create subscription: " + response.getBody());
        }

        return response.getBody().getObject().getString("id"); // preapproval_id
    }

    @Override
    public Customer createCustomer(RegisterPaymentMethodDTO registerPaymentMethodDTO) throws MPException, MPApiException {
        CustomerRequest customerRequest = CustomerRequest.builder()
                .firstName(registerPaymentMethodDTO.getFirstName())
                .lastName(registerPaymentMethodDTO.getLastName())
                .email(registerPaymentMethodDTO.getEmail())
                .build();

        try {
            return new CustomerClient().create(customerRequest);
        } catch (MPApiException e) {
            if (e.getApiResponse().getContent().contains("the customer already exist")) {

                Map<String, Object> filters = new HashMap<>();
                filters.put("email", registerPaymentMethodDTO.getEmail());

                MPSearchRequest searchRequest = MPSearchRequest.builder()
                        .limit(1)
                        .offset(0)
                        .filters(filters)
                        .build();

                MPResultsResourcesPage<Customer> results = new CustomerClient().search(searchRequest);

                if (!results.getResults().isEmpty()) {
                    return results.getResults().getFirst();
                } else {
                    throw new RuntimeException("Cliente já existia, mas não foi encontrado na busca.");
                }
            }

            System.out.println("MPApiException: " + e.getApiResponse().getContent());
            throw e;
        }
    }


    @Override
    public CustomerCard saveCreditCard(RegisterPaymentMethodDTO registerPaymentMethodDTO, Customer customer) throws MPException, MPApiException {
        try {
        CustomerCardIssuer issuer = CustomerCardIssuer.builder()
                .id(registerPaymentMethodDTO.getIssuerId())
                .build();

        CustomerCardCreateRequest cardCreateRequest = CustomerCardCreateRequest.builder()
                .token(registerPaymentMethodDTO.getToken())
                .issuer(issuer)
                .paymentMethodId(registerPaymentMethodDTO.getPaymentMethodId())
                .build();

        return new CustomerCardClient().create(customer.getId(), cardCreateRequest);
        } catch (MPApiException e) {
            System.out.println("MPApiException: " + e.getApiResponse().getContent());
            throw e;
        } catch (MPException e) {
            System.out.println("MPException: " + e.getMessage());
            throw e;
        }
    }

}
