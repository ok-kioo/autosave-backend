package com.signature.autosave.modules.subscription.service;

import com.signature.autosave.infra.components.intermediation.IIntermediationComponent;
import com.signature.autosave.modules.subscription.builder.SubscriptionPlanBuilder;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import com.signature.autosave.modules.subscription.domain.repository.SubscriptionPlanRepository;
import com.signature.autosave.modules.subscription.dto.CreateSubscriptionPlanDTO;
import com.signature.autosave.modules.subscription.dto.SubscriptionPlanResponseDTO;
import com.signature.autosave.modules.subscription.dto.UpdateSubscriptionPlanDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final IIntermediationComponent mpComponent;

    public SubscriptionPlanResponseDTO createSubscriptionPlan(CreateSubscriptionPlanDTO createDTO){
        SubscriptionPlan subscriptionPlan = SubscriptionPlanBuilder.builder()
                    .withName(createDTO.getName())
                    .withPrice(createDTO.getPrice())
                    .withBillingCycle(createDTO.getBillingCycle())
                    .withDescription(createDTO.getDescription())
                    .withTrialDays(createDTO.getTrialDays())
                    .build();

        String preapprovalPlanId = mpComponent.createPreapprovalPlan(subscriptionPlan);
        subscriptionPlan.setPreapprovalPlanId(preapprovalPlanId);

        subscriptionPlanRepository.save(subscriptionPlan);

        return new SubscriptionPlanResponseDTO(subscriptionPlan.getId(), subscriptionPlan.getName(), subscriptionPlan.getPrice(),subscriptionPlan.getBillingCycle(), subscriptionPlan.getTrialDays(), subscriptionPlan.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponseDTO> listSubscription(UUID id){
        return subscriptionPlanRepository.findByIdAndIsActive(id, true).stream()
                .map(subscriptionPlan -> new SubscriptionPlanResponseDTO(subscriptionPlan.getId(), subscriptionPlan.getName(), subscriptionPlan.getPrice(),subscriptionPlan.getBillingCycle(), subscriptionPlan.getTrialDays(), subscriptionPlan.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponseDTO> listSubscriptions(){
        return subscriptionPlanRepository.findByIsActive(true).stream()
                .map(subscriptionPlan -> new SubscriptionPlanResponseDTO(subscriptionPlan.getId(), subscriptionPlan.getName(), subscriptionPlan.getPrice(),subscriptionPlan.getBillingCycle(), subscriptionPlan.getTrialDays(), subscriptionPlan.getCreatedAt()))
                .toList();
    }

    public SubscriptionPlanResponseDTO updateSubscriptionPlan(UpdateSubscriptionPlanDTO updateSubscriptionPlanDTO, UUID id) {
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado"));

        Optional.ofNullable(updateSubscriptionPlanDTO.getName()).ifPresent(subscriptionPlan::setName);
        Optional.ofNullable(updateSubscriptionPlanDTO.getPrice()).ifPresent(subscriptionPlan::setPrice);
        Optional.ofNullable(updateSubscriptionPlanDTO.getBillingCycle()).ifPresent(subscriptionPlan::setBillingCycle);
        Optional.ofNullable(updateSubscriptionPlanDTO.getDescription()).ifPresent(subscriptionPlan::setDescription);
        Optional.ofNullable(updateSubscriptionPlanDTO.getTrialDays()).ifPresent(subscriptionPlan::setTrialDays);

        subscriptionPlanRepository.save(subscriptionPlan);

        return new SubscriptionPlanResponseDTO(subscriptionPlan.getId(), subscriptionPlan.getName(), subscriptionPlan.getPrice(),subscriptionPlan.getBillingCycle(), subscriptionPlan.getTrialDays(), subscriptionPlan.getCreatedAt());
    }

    public void deleteSubscriptionPlan(UUID id) {
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado"));

        subscriptionPlan.setIsActive(false);
        subscriptionPlanRepository.save(subscriptionPlan);
    }
}
