package com.signature.autosave.services.subscription;

import com.signature.autosave.infra.components.intermediation.IGatewayComponent;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import com.signature.autosave.modules.subscription.domain.enums.BillingCycle;
import com.signature.autosave.modules.subscription.domain.repository.SubscriptionPlanRepository;
import com.signature.autosave.modules.subscription.dto.CreateSubscriptionPlanDTO;
import com.signature.autosave.modules.subscription.dto.SubscriptionPlanResponseDTO;
import com.signature.autosave.modules.subscription.dto.UpdateSubscriptionPlanDTO;
import com.signature.autosave.modules.subscription.service.SubscriptionPlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubscriptionPlanServiceTest {
	@Mock
	private SubscriptionPlanRepository subscriptionPlanRepository;
	@Mock
	private IGatewayComponent gatewayComponent;
	@InjectMocks
	private SubscriptionPlanService subscriptionPlanService;

	@Test
	void createSubscriptionPlanShouldSavePlanAndReturnResponse() {
		CreateSubscriptionPlanDTO dto = new CreateSubscriptionPlanDTO(
				"Starter",
				new BigDecimal("29.90"),
				BillingCycle.MONTHLY,
				"Plano inicial",
				7
		);
		UUID generatedId = UUID.randomUUID();

		when(gatewayComponent.createPreapprovalPlan(any(SubscriptionPlan.class))).thenReturn("preapproval-123");
		doAnswer(invocation -> {
			SubscriptionPlan plan = invocation.getArgument(0);
			plan.setId(generatedId);
			return plan;
		}).when(subscriptionPlanRepository).save(any(SubscriptionPlan.class));

		SubscriptionPlanResponseDTO response = subscriptionPlanService.createSubscriptionPlan(dto);

		assertEquals(generatedId, response.id());
		assertEquals("Starter", response.name());
		assertEquals(new BigDecimal("29.90"), response.price());
		assertEquals(BillingCycle.MONTHLY, response.billingCycle());
		assertEquals(7, response.trialDays());
		assertNotNull(response.createdAt());

		verify(gatewayComponent).createPreapprovalPlan(any(SubscriptionPlan.class));
		verify(subscriptionPlanRepository).save(any(SubscriptionPlan.class));
	}

	@Test
	void listSubscriptionShouldReturnPlanWhenFound() {
		UUID id = UUID.randomUUID();
		SubscriptionPlan plan = buildPlan(id, "Pro", new BigDecimal("59.90"), BillingCycle.ANNUALLY, 14);

		when(subscriptionPlanRepository.findByIdAndIsActive(id, true)).thenReturn(Optional.of(plan));

		SubscriptionPlanResponseDTO response = subscriptionPlanService.listSubscription(id);

		assertEquals(id, response.id());
		assertEquals("Pro", response.name());
		assertEquals(new BigDecimal("59.90"), response.price());
		assertEquals(BillingCycle.ANNUALLY, response.billingCycle());
		assertEquals(14, response.trialDays());
	}

	@Test
	void listSubscriptionShouldThrowWhenNotFound() {
		UUID id = UUID.randomUUID();
		when(subscriptionPlanRepository.findByIdAndIsActive(id, true)).thenReturn(Optional.empty());

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> subscriptionPlanService.listSubscription(id));

		assertEquals("Plan not found.", ex.getMessage());
	}

	@Test
	void listSubscriptionsShouldReturnMappedPage() {
		Pageable pageable = PageRequest.of(0, 10);
		SubscriptionPlan plan1 = buildPlan(UUID.randomUUID(), "Starter", new BigDecimal("29.90"), BillingCycle.MONTHLY, 7);
		SubscriptionPlan plan2 = buildPlan(UUID.randomUUID(), "Business", new BigDecimal("99.90"), BillingCycle.ANNUALLY, 30);
		Page<SubscriptionPlan> plansPage = new PageImpl<>(List.of(plan1, plan2), pageable, 2);

		when(subscriptionPlanRepository.findByIsActiveTrue(pageable)).thenReturn(plansPage);

		Page<SubscriptionPlanResponseDTO> response = subscriptionPlanService.listSubscriptions(pageable);

		assertEquals(2, response.getTotalElements());
		assertEquals("Starter", response.getContent().get(0).name());
		assertEquals("Business", response.getContent().get(1).name());
	}

	@Test
	void updateSubscriptionPlanShouldUpdateProvidedFields() {
		UUID id = UUID.randomUUID();
		SubscriptionPlan plan = buildPlan(id, "Starter", new BigDecimal("29.90"), BillingCycle.MONTHLY, 7);
		UpdateSubscriptionPlanDTO dto = new UpdateSubscriptionPlanDTO(
				"Starter Plus",
				new BigDecimal("39.90"),
				BillingCycle.ANNUALLY,
				"Plano atualizado",
				10
		);

		when(subscriptionPlanRepository.findByIdAndIsActive(id, true)).thenReturn(Optional.of(plan));

		SubscriptionPlanResponseDTO response = subscriptionPlanService.updateSubscriptionPlan(dto, id);

		assertEquals("Starter Plus", response.name());
		assertEquals(new BigDecimal("39.90"), response.price());
		assertEquals(BillingCycle.ANNUALLY, response.billingCycle());
		assertEquals(10, response.trialDays());
		verify(subscriptionPlanRepository).save(plan);
	}

	@Test
	void updateSubscriptionPlanShouldThrowWhenNotFound() {
		UUID id = UUID.randomUUID();
		UpdateSubscriptionPlanDTO dto = new UpdateSubscriptionPlanDTO("Name", new BigDecimal("10.00"), BillingCycle.MONTHLY, null, 5);

		when(subscriptionPlanRepository.findByIdAndIsActive(id, true)).thenReturn(Optional.empty());

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> subscriptionPlanService.updateSubscriptionPlan(dto, id));

		assertEquals("Plan not found.", ex.getMessage());
		verify(subscriptionPlanRepository, never()).save(any(SubscriptionPlan.class));
	}

	@Test
	void deleteSubscriptionPlanShouldDisablePlanWhenFound() {
		UUID id = UUID.randomUUID();
		SubscriptionPlan plan = buildPlan(id, "Starter", new BigDecimal("29.90"), BillingCycle.MONTHLY, 7);

		when(subscriptionPlanRepository.findByIdAndIsActive(id, true)).thenReturn(Optional.of(plan));

		subscriptionPlanService.deleteSubscriptionPlan(id);

		verify(subscriptionPlanRepository).setSubscriptionPlanAsNonActive(plan);
	}

	@Test
	void deleteSubscriptionPlanShouldThrowWhenNotFound() {
		UUID id = UUID.randomUUID();
		when(subscriptionPlanRepository.findByIdAndIsActive(id, true)).thenReturn(Optional.empty());

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> subscriptionPlanService.deleteSubscriptionPlan(id));

		assertEquals("Plan not found.", ex.getMessage());
		verify(subscriptionPlanRepository, never()).setSubscriptionPlanAsNonActive(any(SubscriptionPlan.class));
	}

	private SubscriptionPlan buildPlan(UUID id, String name, BigDecimal price, BillingCycle billingCycle, Integer trialDays) {
		SubscriptionPlan plan = new SubscriptionPlan();
		plan.setId(id);
		plan.setName(name);
		plan.setPrice(price);
		plan.setBillingCycle(billingCycle);
		plan.setDescription("description");
		plan.setTrialDays(trialDays);
		plan.setPreapprovalPlanId("preapproval-existing");
		plan.setCreatedAt(LocalDateTime.now());
		plan.setIsActive(true);
		return plan;
	}
}
