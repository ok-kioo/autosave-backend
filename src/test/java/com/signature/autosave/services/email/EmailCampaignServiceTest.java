package com.signature.autosave.services.email;

import com.signature.autosave.infra.components.email.IEmailComponent;
import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import com.signature.autosave.modules.contract.domain.enums.BillingStatus;
import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.email.campaign.domain.repository.EmailCampaignRepository;
import com.signature.autosave.modules.email.campaign.dto.CreateEmailCampaignDTO;
import com.signature.autosave.modules.email.campaign.dto.EmailCampaignResponseDTO;
import com.signature.autosave.modules.email.campaign.service.EmailCampaignService;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignApprovedEvent;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignCreatedEvent;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignDeletedEvent;
import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.email.content.domain.repository.EmailContentRepository;
import com.signature.autosave.modules.email.content.service.events.EmailContentDeletedEvent;
import com.signature.autosave.modules.outbox.service.events.OutboxEmailCampaignSendEvent;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import com.signature.autosave.modules.subscription.domain.enums.BillingCycle;
import com.signature.autosave.modules.subscription.domain.repository.SubscriptionPlanRepository;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.enums.Role;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailCampaignServiceTest {
	@Mock
	private EmailCampaignRepository emailCampaignRepository;
	@Mock
	private EmailContentRepository emailContentRepository;
	@Mock
	private SubscriptionPlanRepository subscriptionPlanRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private IEmailComponent IEmailComponent;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private UserDetails userDetails;
	@InjectMocks
	private EmailCampaignService emailCampaignService;

	@Test
	void createEmailCampaignShouldSaveAndPublishEvent() {
		UUID userId = UUID.randomUUID();
		UUID contentId = UUID.randomUUID();
		UUID campaignId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		SubscriptionPlan currentPlan = buildPlan(UUID.randomUUID(), new BigDecimal("49.90"));
		user.setPlanContract(buildPaidContract(currentPlan));
		EmailContent content = buildEmailContent(contentId, user);
		List<SubscriptionPlan> allowedPlans = List.of(currentPlan, buildPlan(UUID.randomUUID(), new BigDecimal("29.90")));
		CreateEmailCampaignDTO dto = new CreateEmailCampaignDTO(contentId, "preview text");

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(emailContentRepository.findByIdAndIsActiveTrue(contentId)).thenReturn(Optional.of(content));
		when(subscriptionPlanRepository.findEmailCampaignSubscriptionPlans(new BigDecimal("49.90"))).thenReturn(allowedPlans);
		when(emailCampaignRepository.save(any(EmailCampaign.class))).thenAnswer(invocation -> {
			EmailCampaign campaign = invocation.getArgument(0);
			campaign.setId(campaignId);
			return campaign;
		});

		EmailCampaignResponseDTO response = emailCampaignService.createEmailCampaign(dto, userDetails);

		assertEquals(campaignId, response.id());
		assertEquals("preview text", response.textPreview());
		assertEquals(2, response.subscriptionPlans().size());

		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(publisher).publishEvent(captor.capture());
		EmailCampaignCreatedEvent event = assertInstanceOf(EmailCampaignCreatedEvent.class, captor.getValue());
		assertEquals(campaignId, event.emailCampaign());
	}

	@Test
	void createEmailCampaignShouldThrowWhenContentEditorDiffersFromUser() {
		UUID userId = UUID.randomUUID();
		UUID contentId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		User editor = buildUser(UUID.randomUUID(), "editor@mail.com");
		EmailContent content = buildEmailContent(contentId, editor);
		CreateEmailCampaignDTO dto = new CreateEmailCampaignDTO(contentId, "preview text");

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(emailContentRepository.findByIdAndIsActiveTrue(contentId)).thenReturn(Optional.of(content));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> emailCampaignService.createEmailCampaign(dto, userDetails));

		assertEquals("The email content must be created by the user for use in a campaign.", ex.getMessage());
		verify(emailCampaignRepository, never()).save(any(EmailCampaign.class));
	}

	@Test
	void listEmailCampaignShouldReturnWhenFound() {
		UUID id = UUID.randomUUID();
		EmailCampaign campaign = buildCampaign(id, buildEmailContent(UUID.randomUUID(), buildUser(UUID.randomUUID(), "editor@mail.com")));

		when(emailCampaignRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.of(campaign));

		EmailCampaignResponseDTO response = emailCampaignService.listEmailCampaign(id);

		assertEquals(id, response.id());
		assertEquals("preview", response.textPreview());
	}

	@Test
	void listEmailCampaignsAvailableShouldThrowWhenUserPlanIsNotPaid() {
		UUID userId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		SubscriptionPlan plan = buildPlan(UUID.randomUUID(), new BigDecimal("49.90"));
		PlanContract contract = buildPaidContract(plan);
		contract.setStatus(BillingStatus.PENDING);
		user.setPlanContract(contract);
		Pageable pageable = PageRequest.of(0, 10);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> emailCampaignService.listEmailCampaignsAvailable(userDetails, pageable, null));

		assertEquals("You do not have permission to read campaigns within a paid plan contract.", ex.getMessage());
	}

	@Test
	void deleteEmailCampaignShouldDisableAndPublishEvent() {
		UUID userId = UUID.randomUUID();
		UUID campaignId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		EmailCampaign campaign = buildCampaign(campaignId, buildEmailContent(UUID.randomUUID(), user));

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(emailCampaignRepository.findByIdAndIsActiveTrue(campaignId)).thenReturn(Optional.of(campaign));

		emailCampaignService.deleteEmailCampaign(campaignId, userDetails);

		verify(emailCampaignRepository).setEmailCampaignAsNonActive(campaign);
		verify(publisher).publishEvent(any(EmailCampaignDeletedEvent.class));
	}

	@Test
	void handleEmailCampaignEventShouldMakeCampaignAvailableAndPublishSendEvent() {
		UUID campaignId = UUID.randomUUID();
		User editor = buildUser(UUID.randomUUID(), "editor@mail.com");
		EmailCampaign campaign = buildCampaign(campaignId, buildEmailContent(UUID.randomUUID(), editor));

		when(emailCampaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
		when(userRepository.findUsersEligibleForCampaign(campaignId)).thenReturn(List.of("a@mail.com", "b@mail.com"));

		emailCampaignService.handleEmailCampaignEvent(new EmailCampaignApprovedEvent(campaignId));

		assertEquals(true, campaign.isAvailable());
		verify(emailCampaignRepository).save(campaign);
		verify(publisher).publishEvent(any(OutboxEmailCampaignSendEvent.class));
	}

	@Test
	void cascadeDeleteEmailCampaignsShouldDisableAllByContentEditor() {
		UUID editorId = UUID.randomUUID();
		User editor = buildUser(editorId, "editor@mail.com");
		UUID emailContentId = UUID.randomUUID();
		EmailContent content = buildEmailContent(emailContentId, editor);
		EmailCampaign campaign1 = buildCampaign(UUID.randomUUID(), content);
		EmailCampaign campaign2 = buildCampaign(UUID.randomUUID(), content);

		when(emailContentRepository.findById(emailContentId)).thenReturn(Optional.of(content));
		when(emailCampaignRepository.findAllByUserIdAndIsActiveTrue(editorId)).thenReturn(List.of(campaign1, campaign2));

		emailCampaignService.cascadeDeleteEmailCampaigns(new EmailContentDeletedEvent(emailContentId));

		verify(emailCampaignRepository).setEmailCampaignAsNonActive(campaign1);
		verify(emailCampaignRepository).setEmailCampaignAsNonActive(campaign2);
		verify(publisher, org.mockito.Mockito.times(2)).publishEvent(any(EmailCampaignDeletedEvent.class));
	}

	private User buildUser(UUID id, String email) {
		User user = new User();
		user.setId(id);
		user.setName("Kio");
		user.setEmail(email);
		user.setPassword("encoded");
		user.setRole(Role.EDITOR);
		user.setActive(true);
		return user;
	}

	private SubscriptionPlan buildPlan(UUID id, BigDecimal price) {
		SubscriptionPlan plan = new SubscriptionPlan();
		plan.setId(id);
		plan.setName("Plan");
		plan.setPrice(price);
		plan.setBillingCycle(BillingCycle.MONTHLY);
		plan.setTrialDays(7);
		plan.setPreapprovalPlanId("preapproval-id");
		plan.setIsActive(true);
		return plan;
	}

	private PlanContract buildPaidContract(SubscriptionPlan plan) {
		PlanContract contract = new PlanContract();
		contract.setId(UUID.randomUUID());
		contract.setSubscriptionPlan(plan);
		contract.setStatus(BillingStatus.PAID);
		contract.setIsRecurring(true);
		return contract;
	}

	private EmailContent buildEmailContent(UUID id, User editor) {
		EmailContent content = new EmailContent();
		content.setId(id);
		content.setTopic("TECH");
		content.setSubject("subject");
		content.setBody("body");
		content.setEditor(editor);
		content.setCreatedAt(LocalDateTime.now());
		content.setActive(true);
		return content;
	}

	private EmailCampaign buildCampaign(UUID id, EmailContent content) {
		EmailCampaign campaign = new EmailCampaign();
		campaign.setId(id);
		campaign.setTextPreview("preview");
		campaign.setEmailContent(content);
		campaign.setSubscriptionPlans(List.of(buildPlan(UUID.randomUUID(), new BigDecimal("49.90"))));
		campaign.setAvailable(false);
		campaign.setCreatedAt(LocalDateTime.now());
		return campaign;
	}
}
