package com.signature.autosave.modules.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.signature.autosave.infra.components.cache.RedisComponent;
import com.signature.autosave.infra.components.intermediation.MPComponent;
import com.signature.autosave.modules.payment.domain.repository.PaymentRepository;
import com.signature.autosave.modules.payment.dto.CreatePaymentDTO;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final UserRepository userRepository;
    private final MPComponent mpComponent;
    private final RedisComponent redisComponent;
    private final PaymentRepository paymentRepository;

    public void createPayment(CreatePaymentDTO createPaymentDTO, UserDetails userDetails, String idempotencyKey) throws JsonProcessingException, MPException, MPApiException {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(user != createPaymentDTO.getPaymentMethod().getUser()){
            throw new RuntimeException("Usuário não corresponde ao método de pagamento");
        }

        String result = redisComponent.processIdempotentRequest(idempotencyKey);
        if (result != null) {
            throw new RuntimeException("Usuário não corresponde ao método de pagamento");
        }

        switch (createPaymentDTO.getPaymentMethod().getType()) {
            case PIX:
                mpComponent.createPixPayment(createPaymentDTO, idempotencyKey);
                break;
            case CREDIT_CARD:
                mpComponent.createCreditCardPayment(createPaymentDTO, idempotencyKey);
                break;
            default:
                throw new RuntimeException("Método de pagamento não suportado");
        }

        /*String token = null;;
        String holderName = null;

        CreditCardPaymentMethod method = creditCardpaymentMethodRepository
                .findById(createPaymentDTO.getPaymentMethod().getId())
                .orElse(null);

        if (method != null) {
            token = method.getToken();
            holderName = method.getCardHolderName();
        }
        else {
            throw new RuntimeException("Método de pagamento não encontrado");
        }

        HttpResponse<String> planResponse = createSubscriptionPlan(createPaymentDTO, holderName);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(planResponse.getBody());

        HttpResponse<String> subscriptionResponse = createSubscription(createPaymentDTO, token, jsonResponse, user);*/
    }

    private HttpResponse<String> createSubscriptionPlan(CreatePaymentDTO createPaymentDTO, String holderName) {
        HttpResponse<String> planResponse = Unirest.post("https://api.mercadopago.com/preapproval_plan")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + System.getenv("MP_ACCESS_TOKEN"))
                .body(Map.of(
                        "reason", "Autosave - Plano Anual Premium",
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
                                        Map.of("id", createPaymentDTO.getPaymentMethod().getType()),

                                        "payment_methods", List.of(

                                                Map.of("id", holderName)
                                        )
                                ))
                )).asString();

        if (planResponse.getStatus() != 201) {
            throw new RuntimeException("Failed to create subscription plan: " + planResponse.getBody());
        }
        
        return planResponse;
    }

    private HttpResponse<String> createSubscription(CreatePaymentDTO createPaymentDTO, String token, JsonNode jsonResponse, User user) {
        HttpResponse<String> subscriptionResponse = Unirest.post("https://api.mercadopago.com/preapproval")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + System.getenv("MP_ACCESS_TOKEN"))
                .body(Map.of(
                        "preapproval_plan_id", jsonResponse.get("id").asText(),
                        "reason", jsonResponse.get("reason").asText(),
                        "payer_email", user.getEmail(),
                        "card_token_id", token,
                        "back_url", "https://www.youtube.com",
                        "status", "pending",

                        "auto_recurring", jsonResponse.get("auto_recurring").asText()
                        )
                )
                .asString();

        if (subscriptionResponse.getStatus() != 201) {
            throw new RuntimeException("Failed to create subscription: " + subscriptionResponse.getBody());
        }

        return subscriptionResponse;
    }
}

