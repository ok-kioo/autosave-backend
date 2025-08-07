package com.signature.autosave.modules.pay.payment.service;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.infra.components.cache.RedisComponent;
import com.signature.autosave.infra.components.intermediation.MPComponent;
import com.signature.autosave.modules.pay.payment.domain.entity.PaymentEntity;
import com.signature.autosave.modules.pay.payment.domain.repository.PaymentRepository;
import com.signature.autosave.modules.pay.payment.dto.CreatePaymentDTO;
import com.signature.autosave.modules.pay.payment.dto.PaymentResponseDTO;
import com.signature.autosave.modules.pay.payment.dto.Subscription;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PixPaymentMethod;
import com.signature.autosave.modules.pay.paymentmethod.domain.repository.CreditCardPaymentMethodRepository;
import com.signature.autosave.modules.pay.paymentmethod.domain.repository.PixPaymentMethodRepository;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final RedisComponent redisComponent;
    private final MPComponent mpComponent;
    private final UserRepository userRepository;
    private final PixPaymentMethodRepository pixMethod;
    private final CreditCardPaymentMethodRepository creditMethod;
    private final PaymentRepository paymentRepository;

    public PaymentResponseDTO createPayment(CreatePaymentDTO createPaymentDTO, UserDetails userDetails, String idempotencyKey, String plan) throws MPException, MPApiException {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if(user != createPaymentDTO.getPaymentMethod().getUser()){
            throw new RuntimeException("Usuário não corresponde ao método de pagamento");
        }

        String result = redisComponent.processIdempotentRequest(idempotencyKey);
        if (result != null) {
            throw new RuntimeException("Requisição já processada");
        }

        return switch (plan){
            case "premium" -> createPremiumPayment(createPaymentDTO, idempotencyKey);
            case "basic" -> createBasicPayment(createPaymentDTO, idempotencyKey);
            default -> throw new RuntimeException("Plano não encontrado");
        };
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO listPayment(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PaymentEntity paymentEntity = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        if(paymentEntity.getPaymentMethod().getUser().getId() != user.getId()){
            throw new RuntimeException("Usuário não corresponde ao pagamento");

        }

        return new PaymentResponseDTO(
                paymentEntity.getId(),
                paymentEntity.getPaymentMethod(),
                paymentEntity.getMpPayment(),
                paymentEntity.getSubscriptionResponse()
        );
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> listPayments(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return paymentRepository.findAllByUserId(user.getId())
                .stream()
                .map(paymentEntity -> new PaymentResponseDTO(
                        paymentEntity.getId(),
                        paymentEntity.getPaymentMethod(),
                        paymentEntity.getMpPayment(),
                        paymentEntity.getSubscriptionResponse()
                ))
                .toList();
    }

    private PaymentResponseDTO createPremiumPayment(CreatePaymentDTO createPaymentDTO, String idempotencyKey) throws MPException, MPApiException {
        switch (createPaymentDTO.getPaymentMethod().getType()) {
            case PIX:
                pixMethod.findById(createPaymentDTO.getPaymentMethod().getId())
                        .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

                Payment pixPayment = mpComponent.createPixPayment(createPaymentDTO, idempotencyKey);

                PaymentEntity pixPaymentResult = new PaymentEntity();
                pixPaymentResult.setPaymentMethod(createPaymentDTO.getPaymentMethod());
                pixPaymentResult.setMpPayment(pixPayment);
                paymentRepository.save(pixPaymentResult);

                return new PaymentResponseDTO(
                        pixPaymentResult.getId(),
                        createPaymentDTO.getPaymentMethod(),
                        pixPayment,
                        null
                );

            case CREDIT_CARD:
                creditMethod.findById(createPaymentDTO.getPaymentMethod().getId())
                        .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

                Payment creditCardPayment = mpComponent.createCreditCardPayment(createPaymentDTO, idempotencyKey);

                PaymentEntity creditCardPaymentResult = new PaymentEntity();
                creditCardPaymentResult.setPaymentMethod(createPaymentDTO.getPaymentMethod());
                creditCardPaymentResult.setMpPayment(creditCardPayment);
                paymentRepository.save(creditCardPaymentResult);

                return new PaymentResponseDTO(
                        creditCardPaymentResult.getId(),
                        createPaymentDTO.getPaymentMethod(),
                        creditCardPayment,
                        null
                );
        }
        throw new RuntimeException("Tipo de método de pagamento não suportado");
    }

    private PaymentResponseDTO createBasicPayment(CreatePaymentDTO createPaymentDTO, String idempotencyKey) throws MPException, MPApiException {
        switch (createPaymentDTO.getPaymentMethod().getType()) {
            case PIX:
                PixPaymentMethod pix = pixMethod.findById(createPaymentDTO.getPaymentMethod().getId())
                        .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

                Subscription pixPayment = mpComponent.createSubscriptionPlan("pix", "pix", pix, createPaymentDTO, idempotencyKey);

                PaymentEntity pixPaymentResult = new PaymentEntity();
                pixPaymentResult.setPaymentMethod(createPaymentDTO.getPaymentMethod());
                pixPaymentResult.setSubscriptionResponse(pixPayment);
                paymentRepository.save(pixPaymentResult);

                return new PaymentResponseDTO(
                        pixPaymentResult.getId(),
                        createPaymentDTO.getPaymentMethod(),
                        null,
                        pixPayment
                );

            case CREDIT_CARD:
                CreditCardPaymentMethod creditCard = creditMethod.findById(createPaymentDTO.getPaymentMethod().getId())
                        .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

                Subscription creditCardPayment = mpComponent.createSubscriptionPlan("credit_card", creditCard.getCustomerCard().getIssuer().getName(), creditCard, createPaymentDTO, idempotencyKey);

                PaymentEntity creditCardPaymentResult = new PaymentEntity();
                creditCardPaymentResult.setPaymentMethod(createPaymentDTO.getPaymentMethod());
                creditCardPaymentResult.setSubscriptionResponse(creditCardPayment);
                paymentRepository.save(creditCardPaymentResult);

                return new PaymentResponseDTO(
                        creditCardPaymentResult.getId(),
                        createPaymentDTO.getPaymentMethod(),
                        null,
                        creditCardPayment
                );
        }
        throw new RuntimeException("Tipo de método de pagamento não suportado");
    }


}

