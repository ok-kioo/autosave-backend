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
import com.signature.autosave.modules.payment.method.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.payment.method.domain.entity.PaymentMethod;
import com.signature.autosave.modules.payment.method.domain.entity.PixPaymentMethod;
import com.signature.autosave.modules.payment.method.domain.repository.PaymentMethodRepository;
import com.signature.autosave.modules.payment.payload.domain.entity.Payload;
import com.signature.autosave.modules.payment.payload.domain.repository.PayloadRepository;
import com.signature.autosave.modules.payment.payload.service.event.PayloadCreateEvent;
import com.signature.autosave.modules.payment.payload.service.event.PayloadRefundEvent;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import com.signature.autosave.modules.subscription.domain.enums.BillingCycle;
import com.signature.autosave.modules.subscription.domain.repository.SubscriptionPlanRepository;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    private final PayloadRepository payloadRepository;

    public PlanContractResponseDTO createPlanContract(CreatePlanContractDTO createPlanContractDTO, UserDetails userDetails, String idempotencyKey) throws MPException, MPApiException {
        String result = redisComponent.processIdempotentRequest(idempotencyKey);
        if (result != null) {
            throw new RuntimeException("Requisição já processada");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(createPlanContractDTO.paymentMethod())
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findByIdAndIsActive(createPlanContractDTO.subscriptionPlan(), true)
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));

        if (user != paymentMethod.getUser()) {
            throw new RuntimeException("Usuário não corresponde ao método de pagamento");
        }

        return switch (subscriptionPlan.getBillingCycle()) {
            case ANNUALLY ->
                    createAnnuallyPayment(paymentMethod, subscriptionPlan, createPlanContractDTO.installments(), idempotencyKey);
            case MONTHLY -> createMonthlyPayment(paymentMethod, subscriptionPlan, idempotencyKey);
        };
    }

    public PlanContractResponseDTO cancelPlanContract(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PlanContract planContract = planContractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

        if (user.getPlanContract() == null || user.getPlanContract().getId() != planContract.getId()) {
            throw new RuntimeException("Usuário não corresponde ao contrato");
        }

        if (planContract.getIsRecurring() && planContract.getEndsAt().isAfter(LocalDate.now())) {
            mpComponent.cancelSubscription(planContract.getContractId());
        } else {
            throw new RuntimeException("Não é possível cancelar essa assinatura");
        }

        planContract.setStatus(BillingStatus.CANCELED);
        planContractRepository.save(planContract);

        return planContractResponseBuild(planContract);
    }

    public PlanContractResponseDTO refundPlanContract(UUID id, UserDetails userDetails) throws MPException, MPApiException {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PlanContract planContract = planContractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

        if (user.getPlanContract() == null || user.getPlanContract().getId() != planContract.getId()) {
            throw new RuntimeException("Usuário não corresponde ao contrato");
        }

        if (planContract.getEndsAt().isBefore(LocalDate.now())) {
            throw new RuntimeException("Não é possível reembolsar um pagamento vencido");
        }

        boolean lessThan30Days =
                ChronoUnit.DAYS.between(planContract.getStartedAt(), LocalDate.now()) < 30;

        if (!lessThan30Days) {
            throw new RuntimeException("Não é possível reembolsar um pagamento com mais de 30 dias");
        }

        if (planContract.getSubscriptionPlan().getBillingCycle() == BillingCycle.ANNUALLY) {
            mpComponent.refundPayment(Long.valueOf(planContract.getContractId()));

        }

        if (planContract.getSubscriptionPlan().getBillingCycle() == BillingCycle.MONTHLY) {
            Payload payload = payloadRepository.findByPlanContract(planContract)
                    .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

            mpComponent.refundPayment(payload.getPaymentId());
        }

        planContract.setStatus(BillingStatus.REFUNDED);
        planContractRepository.save(planContract);

        return planContractResponseBuild(planContract);
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
        PlanContract planContract = PlanContractBuilder.builder()
                .withPaymentMethod(paymentMethod)
                .withSubscriptionPlan(subscriptionPlan)
                .withStatus(BillingStatus.PENDING)
                .withIsRecurring(false)
                .withStartedAt(null)
                .withEndsAt(null)
                .build();

        planContractRepository.save(planContract);

        if (paymentMethod instanceof PixPaymentMethod pixPaymentMethod) {
            Payment pixPayment = mpComponent.createPixPayment(pixPaymentMethod, subscriptionPlan, planContract, idempotencyKey);
            planContract.setContractId(String.valueOf(pixPayment.getId()));
            planContractRepository.save(planContract);

            return planContractResponseBuild(planContract);

        } else if (paymentMethod instanceof CreditCardPaymentMethod creditCardPaymentMethod) {
            Payment creditCardPayment = mpComponent.createCreditCardPayment(creditCardPaymentMethod, subscriptionPlan, planContract, installments, idempotencyKey);
            planContract.setContractId(String.valueOf(creditCardPayment.getId()));
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
            PlanContract planContract = PlanContractBuilder.builder()
                    .withPaymentMethod(paymentMethod)
                    .withSubscriptionPlan(subscriptionPlan)
                    .withStatus(BillingStatus.PENDING)
                    .withIsRecurring(false)
                    .withStartedAt(null)
                    .withEndsAt(null)
                    .build();

            planContractRepository.save(planContract);

            String preaprovalId = mpComponent.createSubscription(subscriptionPlan.getPreapprovalPlanId(), creditCardPaymentMethod, planContract, idempotencyKey);
            planContract.setContractId(preaprovalId);

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

    @EventListener
    public void onPayloadCreated(PayloadCreateEvent event) {
        PlanContract planContract = planContractRepository.findById(event.planContract())
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

        planContract.setStatus(BillingStatus.PAID);
        planContract.setStartedAt(LocalDate.now());
        if (planContract.getSubscriptionPlan().getBillingCycle() == BillingCycle.ANNUALLY) {
            planContract.setEndsAt(LocalDate.now().plusYears(1));
        } else if (planContract.getSubscriptionPlan().getBillingCycle() == BillingCycle.MONTHLY) {
            planContract.setEndsAt(LocalDate.now().plusMonths(1));
        }
        planContractRepository.save(planContract);
    }

    @EventListener
    public void onPayloadRefund(PayloadRefundEvent event) {
        PlanContract planContract = planContractRepository.findById(event.planContract())
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

        planContract.setStatus(BillingStatus.REFUNDED);
        if(planContract.getSubscriptionPlan().getBillingCycle().equals(BillingCycle.ANNUALLY)) {
            mpComponent.cancelSubscription(planContract.getSubscriptionPlan().getPreapprovalPlanId());
        }

        planContractRepository.save(planContract);
    }
}

