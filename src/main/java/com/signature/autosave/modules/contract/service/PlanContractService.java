package com.signature.autosave.modules.contract.service;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.infra.components.cache.ICacheComponent;
import com.signature.autosave.infra.components.intermediation.IIntermediationComponent;
import com.signature.autosave.modules.contract.builder.PlanContractBuilder;
import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import com.signature.autosave.modules.contract.domain.enums.BillingStatus;
import com.signature.autosave.modules.contract.domain.repository.PlanContractRepository;
import com.signature.autosave.modules.contract.dto.CreatePlanContractDTO;
import com.signature.autosave.modules.contract.dto.PlanContractResponseDTO;
import com.signature.autosave.modules.payment.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.payment.domain.entity.PaymentMethod;
import com.signature.autosave.modules.payment.domain.entity.PixPaymentMethod;
import com.signature.autosave.modules.payment.domain.repository.PaymentMethodRepository;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import com.signature.autosave.modules.subscription.domain.repository.SubscriptionPlanRepository;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanContractService {
    private final ICacheComponent redisComponent;
    private final IIntermediationComponent mpComponent;
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PlanContractRepository planContractRepository;

    public PlanContractResponseDTO createPayment(CreatePlanContractDTO createPlanContractDTO, UserDetails userDetails, String idempotencyKey) throws MPException, MPApiException {
        String result = redisComponent.processIdempotentRequest(idempotencyKey);
        if (result != null) {
            throw new RuntimeException("Requisição já processada");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(createPlanContractDTO.getPaymentMethod())
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findByIdAndIsActive(createPlanContractDTO.getSubscriptionPlan(), true)
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));

        if (user != paymentMethod.getUser()) {
            throw new RuntimeException("Usuário não corresponde ao método de pagamento");
        }

        return switch (subscriptionPlan.getBillingCycle()) {
            case ANNUALLY ->
                    createAnnuallyPayment(paymentMethod, subscriptionPlan, createPlanContractDTO.getInstallments(), idempotencyKey);
            case MONTHLY -> createMonthlyPayment(paymentMethod, subscriptionPlan, idempotencyKey);
        };
    }

    @Transactional(readOnly = true)
    public PlanContractResponseDTO listPlanContract(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PlanContract planContract = planContractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        if (planContract.getPaymentMethod().getUser().getId() != user.getId()) {
            throw new RuntimeException("Usuário não corresponde ao pagamento");

        }

        return planContractResponseBuild(planContract);
    }

    @Transactional(readOnly = true)
    public List<PlanContractResponseDTO> listPlanContracts(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return planContractRepository.findAllByUserId(user.getId())
                .stream()
                .map(this::planContractResponseBuild)
                .toList();
    }

    private PlanContractResponseDTO createAnnuallyPayment(PaymentMethod paymentMethod, SubscriptionPlan subscriptionPlan, Integer installments, String idempotencyKey) throws MPException, MPApiException {
        if (paymentMethod instanceof PixPaymentMethod pixPaymentMethod) {
            Payment pixPayment = mpComponent.createPixPayment(pixPaymentMethod, subscriptionPlan, idempotencyKey);

            PlanContract planContract = PlanContractBuilder.builder()
                    .withPaymentMethod(pixPaymentMethod)
                    .withSubscriptionPlan(subscriptionPlan)
                    .withContractId(String.valueOf(pixPayment.getId()))
                    .withStatus(BillingStatus.PAID)
                    .withIsRecurring(false)
                    .withStartedAt(pixPayment.getDateCreated().toLocalDateTime())
                    .withEndsAt(pixPayment.getDateCreated().toLocalDateTime().plusDays(365))
                    .build();

            planContractRepository.save(planContract);

            return planContractResponseBuild(planContract);
        } else if (paymentMethod instanceof CreditCardPaymentMethod creditCardPaymentMethod) {
            Payment creditCardPayment = mpComponent.createCreditCardPayment(creditCardPaymentMethod, subscriptionPlan, installments, idempotencyKey);

            PlanContract planContract = PlanContractBuilder.builder()
                    .withPaymentMethod(creditCardPaymentMethod)
                    .withSubscriptionPlan(subscriptionPlan)
                    .withContractId(String.valueOf(creditCardPayment.getId()))
                    .withStatus(BillingStatus.PAID)
                    .withIsRecurring(false)
                    .withStartedAt(creditCardPayment.getDateCreated().toLocalDateTime())
                    .withEndsAt(creditCardPayment.getDateCreated().toLocalDateTime().plusDays(365))
                    .build();

            planContractRepository.save(planContract);

            return planContractResponseBuild(planContract);
        }

        throw new RuntimeException("Tipo de método de pagamento não suportado");
    }

    private PlanContractResponseDTO createMonthlyPayment(PaymentMethod paymentMethod, SubscriptionPlan subscriptionPlan, String idempotencyKey) throws MPException, MPApiException {
        if (paymentMethod instanceof PixPaymentMethod) {
            throw new IllegalArgumentException("Método de pagamento não suportado para pagamentos mensais");
        }

        if (paymentMethod instanceof CreditCardPaymentMethod creditCardPaymentMethod) {
            String preaprovalId = mpComponent.createSubscription(subscriptionPlan.getPreapprovalPlanId(), creditCardPaymentMethod, idempotencyKey);

            PlanContract planContract = PlanContractBuilder.builder()
                    .withPaymentMethod(creditCardPaymentMethod)
                    .withSubscriptionPlan(subscriptionPlan)
                    .withContractId(preaprovalId)
                    .withStatus(BillingStatus.PAID)
                    .withIsRecurring(true)
                    .withStartedAt(LocalDateTime.now())
                    .withEndsAt(LocalDateTime.now().plusDays(30))
                    .build();

            planContractRepository.save(planContract);

            return planContractResponseBuild(planContract);
        }
        throw new RuntimeException("Tipo de método de pagamento não suportado");
    }

    private PlanContractResponseDTO planContractResponseBuild(PlanContract planContract) {
        return new PlanContractResponseDTO(
                planContract.getId(),
                planContract.getSubscriptionPlan(),
                planContract.getPaymentMethod(),
                planContract.getContractId(),
                planContract.getStatus(),
                planContract.getIsRecurring(),
                planContract.getStartedAt(),
                planContract.getEndsAt()
        );
    }

}

