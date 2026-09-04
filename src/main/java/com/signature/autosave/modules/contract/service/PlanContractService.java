package com.signature.autosave.modules.contract.service;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.infra.components.cache.ICacheComponent;
import com.signature.autosave.infra.components.intermediation.IGatewayComponent;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanContractService {
    private final ICacheComponent redisComponent;
    private final IGatewayComponent gatewayComponent;
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PlanContractRepository planContractRepository;
    private final PayloadRepository payloadRepository;

    @Transactional
    public PlanContractResponseDTO createPlanContract(CreatePlanContractDTO createPlanContractDTO, UserDetails userDetails, String idempotencyKey) throws MPException, MPApiException {
        String result = redisComponent.processIdempotentRequest(idempotencyKey);
        if (result != null) {
            throw new RuntimeException("Requisition already processed.");
        }

        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found."));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(createPlanContractDTO.paymentMethod())
                .orElseThrow(() -> new RuntimeException("Payment method not found."));

        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findByIdAndIsActive(createPlanContractDTO.subscriptionPlan(), true)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found."));

        if (user != paymentMethod.getUser()) {
            throw new RuntimeException("You do not have permission to use this payment method.");
        }

        return switch (subscriptionPlan.getBillingCycle()) {
            case ANNUALLY ->
                    createAnnuallyPayment(paymentMethod, subscriptionPlan, createPlanContractDTO.installments(), idempotencyKey);
            case MONTHLY -> createMonthlyPayment(paymentMethod, subscriptionPlan, idempotencyKey);
        };
    }

    @Transactional
    public PlanContractResponseDTO cancelPlanContract(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found."));

        PlanContract planContract = planContractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found."));

        if (user.getPlanContract() == null || user.getPlanContract().getId() != planContract.getId()) {
            throw new RuntimeException("You do not have permission to cancel this contract.");
        }

        if (planContract.getIsRecurring() && planContract.getEndsAt() != null && planContract.getEndsAt().isAfter(LocalDate.now())) {
            gatewayComponent.cancelSubscription(planContract.getContractId());

        } else {
            throw new RuntimeException("You cannot cancel a contract that is not recurring or has already expired.");
        }

        planContract.setStatus(BillingStatus.CANCELED);
        planContractRepository.save(planContract);

        return planContractResponseBuild(planContract);
    }

    @Transactional
    public PlanContractResponseDTO refundPlanContract(UUID id, UserDetails userDetails) throws MPException, MPApiException {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found."));

        PlanContract planContract = planContractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found."));

        if (user.getPlanContract() == null || user.getPlanContract().getId() != planContract.getId()) {
            throw new RuntimeException("You do not have permission to refund this contract.");
        }

        if (planContract.getEndsAt().isBefore(LocalDate.now())) {
            throw new RuntimeException("It is not possible to refund an overdue payment.");
        }

        boolean lessThan30Days =
                ChronoUnit.DAYS.between(planContract.getStartedAt(), LocalDate.now()) < 30;

        if (!lessThan30Days) {
            throw new RuntimeException("It is not possible to refund a payment older than 30 days.");
        }

        if (planContract.getSubscriptionPlan().getBillingCycle() == BillingCycle.ANNUALLY) {
            gatewayComponent.refundPayment(Long.valueOf(planContract.getContractId()));

        }

        if (planContract.getSubscriptionPlan().getBillingCycle() == BillingCycle.MONTHLY) {
            Payload payload = payloadRepository.findByPlanContract(planContract)
                    .orElseThrow(() -> new RuntimeException("Payment not found."));

            gatewayComponent.refundPayment(payload.getPaymentId());
        }

        planContract.setStatus(BillingStatus.REFUNDED);
        planContractRepository.save(planContract);

        return planContractResponseBuild(planContract);
    }

    @Transactional(readOnly = true)
    public PlanContractResponseDTO listPlanContract(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found."));

        PlanContract planContract = planContractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found."));

        if (planContract.getPaymentMethod().getUser().getId() != user.getId()) {
            throw new RuntimeException("You do not have permission to read this contract.");

        }

        return planContractResponseBuild(planContract);
    }

    @Transactional(readOnly = true)
    public Page<PlanContractResponseDTO> listPlanContracts(UserDetails userDetails, Pageable pageable) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found."));

        return planContractRepository.findAllByUserId(user.getId(), pageable)
                .map(this::planContractResponseBuild);
    }

    @Transactional
    private PlanContractResponseDTO createAnnuallyPayment(PaymentMethod paymentMethod, SubscriptionPlan subscriptionPlan,
                                                          Integer installments, String idempotencyKey) throws MPException, MPApiException {
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
            Payment pixPayment = gatewayComponent.createPixPayment(pixPaymentMethod, subscriptionPlan, planContract, idempotencyKey);
            planContract.setContractId(String.valueOf(pixPayment.getId()));
            planContractRepository.save(planContract);

            return planContractResponseBuild(planContract);

        } else if (paymentMethod instanceof CreditCardPaymentMethod creditCardPaymentMethod) {
            Payment creditCardPayment = gatewayComponent.createCreditCardPayment(creditCardPaymentMethod, subscriptionPlan,
                    planContract, installments, idempotencyKey);
            planContract.setContractId(String.valueOf(creditCardPayment.getId()));
            planContractRepository.save(planContract);

            return planContractResponseBuild(planContract);
        }

        throw new RuntimeException("Unsupported payment method type.");
    }

    @Transactional
    private PlanContractResponseDTO createMonthlyPayment(PaymentMethod paymentMethod, SubscriptionPlan subscriptionPlan,
                                                         String idempotencyKey) throws MPException, MPApiException {
        if (paymentMethod instanceof PixPaymentMethod) {
            throw new IllegalArgumentException("Unsupported payment method type.");
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

            String preaprovalId = gatewayComponent.createSubscription(subscriptionPlan.getPreapprovalPlanId(), creditCardPaymentMethod, planContract, idempotencyKey);
            planContract.setContractId(preaprovalId);

            planContractRepository.save(planContract);

            return planContractResponseBuild(planContract);
        }
        throw new RuntimeException("Unsupported payment method type.");
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
                .orElseThrow(() -> new RuntimeException("Contract not found."));

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
                .orElseThrow(() -> new RuntimeException("Contract not found."));

        planContract.setStatus(BillingStatus.REFUNDED);
        if(planContract.getSubscriptionPlan().getBillingCycle().equals(BillingCycle.ANNUALLY)) {
            gatewayComponent.cancelSubscription(planContract.getSubscriptionPlan().getPreapprovalPlanId());
        }

        planContractRepository.save(planContract);
    }
}

