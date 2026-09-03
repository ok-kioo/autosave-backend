package com.signature.autosave.modules.subscription.service;

import com.signature.autosave.infra.components.intermediation.IGatewayComponent;
import com.signature.autosave.modules.subscription.builder.SubscriptionPlanBuilder;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import com.signature.autosave.modules.subscription.domain.repository.SubscriptionPlanRepository;
import com.signature.autosave.modules.subscription.dto.CreateSubscriptionPlanDTO;
import com.signature.autosave.modules.subscription.dto.SubscriptionPlanResponseDTO;
import com.signature.autosave.modules.subscription.dto.UpdateSubscriptionPlanDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final IGatewayComponent gatewayComponent;

    @Transactional
    public SubscriptionPlanResponseDTO createSubscriptionPlan(CreateSubscriptionPlanDTO createDTO){
        SubscriptionPlan subscriptionPlan = SubscriptionPlanBuilder.builder()
                    .withName(createDTO.name())
                    .withPrice(createDTO.price())
                    .withBillingCycle(createDTO.billingCycle())
                    .withDescription(createDTO.description())
                    .withTrialDays(createDTO.trialDays())
                    .build();

        String preapprovalPlanId = gatewayComponent.createPreapprovalPlan(subscriptionPlan);
        subscriptionPlan.setPreapprovalPlanId(preapprovalPlanId);

        subscriptionPlanRepository.save(subscriptionPlan);

        return new SubscriptionPlanResponseDTO(subscriptionPlan.getId(), subscriptionPlan.getName(),
                subscriptionPlan.getPrice(),subscriptionPlan.getBillingCycle(), subscriptionPlan.getTrialDays(),
                subscriptionPlan.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public SubscriptionPlanResponseDTO listSubscription(UUID id){
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found."));

        return new SubscriptionPlanResponseDTO(subscriptionPlan.getId(), subscriptionPlan.getName(),
                subscriptionPlan.getPrice(),subscriptionPlan.getBillingCycle(), subscriptionPlan.getTrialDays(),
                subscriptionPlan.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionPlanResponseDTO> listSubscriptions(Pageable pageable) {
        return subscriptionPlanRepository
                .findByIsActiveTrue(pageable)
                .map(subscriptionPlan -> new SubscriptionPlanResponseDTO(
                        subscriptionPlan.getId(),
                        subscriptionPlan.getName(),
                        subscriptionPlan.getPrice(),
                        subscriptionPlan.getBillingCycle(),
                        subscriptionPlan.getTrialDays(),
                        subscriptionPlan.getCreatedAt()
                ));
    }

    @Transactional
    public SubscriptionPlanResponseDTO updateSubscriptionPlan(UpdateSubscriptionPlanDTO updateSubscriptionPlanDTO, UUID id) {
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found."));

        Optional.ofNullable(updateSubscriptionPlanDTO.name()).ifPresent(subscriptionPlan::setName);
        Optional.ofNullable(updateSubscriptionPlanDTO.price()).ifPresent(subscriptionPlan::setPrice);
        Optional.ofNullable(updateSubscriptionPlanDTO.billingCycle()).ifPresent(subscriptionPlan::setBillingCycle);
        Optional.ofNullable(updateSubscriptionPlanDTO.description()).ifPresent(subscriptionPlan::setDescription);
        Optional.ofNullable(updateSubscriptionPlanDTO.trialDays()).ifPresent(subscriptionPlan::setTrialDays);

        subscriptionPlanRepository.save(subscriptionPlan);

        return new SubscriptionPlanResponseDTO(subscriptionPlan.getId(), subscriptionPlan.getName(),
                subscriptionPlan.getPrice(),subscriptionPlan.getBillingCycle(), subscriptionPlan.getTrialDays(),
                subscriptionPlan.getCreatedAt());
    }

    @Transactional
    public void deleteSubscriptionPlan(UUID id) {
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found."));

        subscriptionPlanRepository.setSubscriptionPlanAsNonActive(subscriptionPlan);
    }
}
