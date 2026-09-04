package com.signature.autosave.services.contract;

import com.signature.autosave.infra.components.cache.ICacheComponent;
import com.signature.autosave.infra.components.intermediation.IGatewayComponent;
import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import com.signature.autosave.modules.contract.domain.enums.BillingStatus;
import com.signature.autosave.modules.contract.domain.repository.PlanContractRepository;
import com.signature.autosave.modules.contract.dto.CreatePlanContractDTO;
import com.signature.autosave.modules.contract.dto.PlanContractResponseDTO;
import com.signature.autosave.modules.contract.service.PlanContractService;
import com.signature.autosave.modules.payment.method.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.payment.method.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.payment.method.domain.repository.PaymentMethodRepository;
import com.signature.autosave.modules.payment.payload.domain.repository.PayloadRepository;
import com.signature.autosave.modules.payment.payload.service.event.PayloadCreateEvent;
import com.signature.autosave.modules.payment.payload.service.event.PayloadRefundEvent;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import com.signature.autosave.modules.subscription.domain.enums.BillingCycle;
import com.signature.autosave.modules.subscription.domain.repository.SubscriptionPlanRepository;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.enums.Role;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlanContractServiceTest {
	@Mock
	private ICacheComponent redisComponent;
	@Mock
	private IGatewayComponent gatewayComponent;
	@Mock
	private UserRepository userRepository;
	@Mock
	private PaymentMethodRepository paymentMethodRepository;
	@Mock
	private SubscriptionPlanRepository subscriptionPlanRepository;
	@Mock
	private PlanContractRepository planContractRepository;
	@Mock
	private PayloadRepository payloadRepository;
	@Mock
	private UserDetails userDetails;
	@InjectMocks
	private PlanContractService planContractService;

	@Test
	void createPlanContractShouldThrowWhenRequestIsIdempotentDuplicate() {
		UUID paymentMethodId = UUID.randomUUID();
		UUID subscriptionPlanId = UUID.randomUUID();
		CreatePlanContractDTO dto = new CreatePlanContractDTO(paymentMethodId, 1, subscriptionPlanId);

		when(redisComponent.processIdempotentRequest("idem-key")).thenReturn("already-processed");

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> planContractService.createPlanContract(dto, userDetails, "idem-key"));

		assertEquals("Requisition already processed.", ex.getMessage());
		verify(userRepository, never()).findByEmailAndIsActiveTrue(any());
	}

	@Test
	void createPlanContractShouldCreateMonthlyContractWithCreditCard() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID paymentMethodId = UUID.randomUUID();
		UUID subscriptionPlanId = UUID.randomUUID();
		UUID generatedContractId = UUID.randomUUID();

		User user = buildUser(userId, "kio@mail.com");
		CreditCardPaymentMethod paymentMethod = buildCreditCardPaymentMethod(paymentMethodId, user);
		SubscriptionPlan plan = buildSubscriptionPlan(subscriptionPlanId, BillingCycle.MONTHLY);
		CreatePlanContractDTO dto = new CreatePlanContractDTO(paymentMethodId, 1, subscriptionPlanId);

		when(redisComponent.processIdempotentRequest("idem-key")).thenReturn(null);
		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
		when(subscriptionPlanRepository.findByIdAndIsActive(subscriptionPlanId, true)).thenReturn(Optional.of(plan));
		doAnswer(invocation -> {
			PlanContract contract = invocation.getArgument(0);
			if (contract.getId() == null) {
				contract.setId(generatedContractId);
			}
			return contract;
		}).when(planContractRepository).save(any(PlanContract.class));
		when(gatewayComponent.createSubscription(
				eq(plan.getPreapprovalPlanId()),
				eq(paymentMethod),
				any(PlanContract.class),
				eq("idem-key")))
				.thenReturn("preapproval-contract-id");

		PlanContractResponseDTO response = planContractService.createPlanContract(dto, userDetails, "idem-key");

		assertEquals(generatedContractId, response.id());
		assertEquals("preapproval-contract-id", response.contractId());
		assertEquals(BillingStatus.PENDING, response.status());
		verify(planContractRepository, org.mockito.Mockito.times(2)).save(any(PlanContract.class));
	}

	@Test
	void cancelPlanContractShouldCancelRecurringContract() {
		UUID userId = UUID.randomUUID();
		UUID contractId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		PlanContract contract = buildPlanContract(contractId, BillingCycle.MONTHLY, true, LocalDate.now().plusDays(10));
		contract.setContractId("subscription-123");
		user.setPlanContract(contract);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(planContractRepository.findById(contractId)).thenReturn(Optional.of(contract));

		PlanContractResponseDTO response = planContractService.cancelPlanContract(contractId, userDetails);

		assertEquals(BillingStatus.CANCELED, response.status());
		verify(gatewayComponent).cancelSubscription("subscription-123");
		verify(planContractRepository).save(contract);
	}

	@Test
	void refundPlanContractShouldRefundAnnuallyContractWithinThirtyDays() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID contractId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		PlanContract contract = buildPlanContract(contractId, BillingCycle.ANNUALLY, false, LocalDate.now().plusDays(20));
		contract.setStartedAt(LocalDate.now().minusDays(5));
		contract.setContractId("987654");
		user.setPlanContract(contract);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(planContractRepository.findById(contractId)).thenReturn(Optional.of(contract));

		PlanContractResponseDTO response = planContractService.refundPlanContract(contractId, userDetails);

		assertEquals(BillingStatus.REFUNDED, response.status());
		verify(gatewayComponent).refundPayment(987654L);
		verify(planContractRepository).save(contract);
	}

	@Test
	void listPlanContractShouldReturnContractWhenUserHasPermission() {
		UUID userId = UUID.randomUUID();
		UUID contractId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		PlanContract contract = buildPlanContract(contractId, BillingCycle.MONTHLY, true, LocalDate.now().plusDays(30));
		contract.getPaymentMethod().setUser(user);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(planContractRepository.findById(contractId)).thenReturn(Optional.of(contract));

		PlanContractResponseDTO response = planContractService.listPlanContract(contractId, userDetails);

		assertEquals(contractId, response.id());
		assertEquals(contract.getPaymentMethod().getId(), response.paymentMethod().getId());
	}

	@Test
	void listPlanContractsShouldReturnMappedPage() {
		UUID userId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		Pageable pageable = PageRequest.of(0, 10);
		PlanContract contract = buildPlanContract(UUID.randomUUID(), BillingCycle.MONTHLY, true, LocalDate.now().plusDays(30));
		Page<PlanContract> page = new PageImpl<>(List.of(contract), pageable, 1);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(planContractRepository.findAllByUserId(userId, pageable)).thenReturn(page);

		Page<PlanContractResponseDTO> response = planContractService.listPlanContracts(userDetails, pageable);

		assertEquals(1, response.getTotalElements());
		assertEquals(contract.getId(), response.getContent().get(0).id());
	}

	@Test
	void onPayloadCreatedShouldMarkContractAsPaidAndSetEndDate() {
		UUID contractId = UUID.randomUUID();
		PlanContract contract = buildPlanContract(contractId, BillingCycle.ANNUALLY, false, null);

		when(planContractRepository.findById(contractId)).thenReturn(Optional.of(contract));

		planContractService.onPayloadCreated(new PayloadCreateEvent(contractId));

		assertEquals(BillingStatus.PAID, contract.getStatus());
		assertEquals(LocalDate.now().plusYears(1), contract.getEndsAt());
		verify(planContractRepository).save(contract);
	}

	@Test
	void onPayloadRefundShouldMarkAsRefundedAndCancelAnnualSubscription() {
		UUID contractId = UUID.randomUUID();
		PlanContract contract = buildPlanContract(contractId, BillingCycle.ANNUALLY, true, LocalDate.now().plusDays(20));

		when(planContractRepository.findById(contractId)).thenReturn(Optional.of(contract));

		planContractService.onPayloadRefund(new PayloadRefundEvent(contractId));

		assertEquals(BillingStatus.REFUNDED, contract.getStatus());
		verify(gatewayComponent).cancelSubscription(contract.getSubscriptionPlan().getPreapprovalPlanId());
		verify(planContractRepository).save(contract);
	}

	private User buildUser(UUID id, String email) {
		User user = new User();
		user.setId(id);
		user.setName("Kio");
		user.setEmail(email);
		user.setPassword("encoded-password");
		user.setRole(Role.VIEWER);
		user.setActive(true);
		return user;
	}

	private CreditCardPaymentMethod buildCreditCardPaymentMethod(UUID id, User user) {
		CreditCardPaymentMethod method = new CreditCardPaymentMethod();
		method.setId(id);
		method.setType(PaymentMethodType.CREDIT_CARD);
		method.setFirstName("Kio");
		method.setLastName("Silva");
		method.setDocumentNumber("12345678900");
		method.setDefault(true);
		method.setActive(true);
		method.setUser(user);
		method.setCustomerId("customer-1");
		method.setCustomerCardId("card-1");
		return method;
	}

	private SubscriptionPlan buildSubscriptionPlan(UUID id, BillingCycle billingCycle) {
		SubscriptionPlan plan = new SubscriptionPlan();
		plan.setId(id);
		plan.setName("Plan");
		plan.setBillingCycle(billingCycle);
		plan.setTrialDays(7);
		plan.setPreapprovalPlanId("preapproval-plan-id");
		plan.setIsActive(true);
		return plan;
	}

	private PlanContract buildPlanContract(UUID id, BillingCycle billingCycle, boolean recurring, LocalDate endsAt) {
		User user = buildUser(UUID.randomUUID(), "holder@mail.com");
		CreditCardPaymentMethod method = buildCreditCardPaymentMethod(UUID.randomUUID(), user);
		SubscriptionPlan plan = buildSubscriptionPlan(UUID.randomUUID(), billingCycle);

		PlanContract contract = new PlanContract();
		contract.setId(id);
		contract.setPaymentMethod(method);
		contract.setSubscriptionPlan(plan);
		contract.setContractId("contract-id");
		contract.setStatus(BillingStatus.PENDING);
		contract.setIsRecurring(recurring);
		contract.setStartedAt(LocalDate.now().minusDays(2));
		contract.setEndsAt(endsAt);
		return contract;
	}
}
