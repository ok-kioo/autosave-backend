package com.signature.autosave.services.email;

import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaignReview;
import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;
import com.signature.autosave.modules.email.campaign.domain.repository.EmailCampaignRepository;
import com.signature.autosave.modules.email.campaign.domain.repository.EmailCampaignReviewRepository;
import com.signature.autosave.modules.email.campaign.dto.CreateEmailCampaignReviewDTO;
import com.signature.autosave.modules.email.campaign.dto.EmailCampaignReviewResponseDTO;
import com.signature.autosave.modules.email.campaign.dto.UpdateEmailCampaignReviewDTO;
import com.signature.autosave.modules.email.campaign.service.EmailCampaignReviewService;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignDeletedEvent;
import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.enums.Role;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailCampaignReviewServiceTest {
	@Mock
	private EmailCampaignReviewRepository emailCampaignReviewRepository;
	@Mock
	private EmailCampaignRepository emailCampaignRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private UserDetails userDetails;
	@InjectMocks
	private EmailCampaignReviewService emailCampaignReviewService;

	@Test
	void createEmailCampaignReviewShouldSaveAndReturnResponse() {
		UUID userId = UUID.randomUUID();
		UUID campaignId = UUID.randomUUID();
		UUID reviewId = UUID.randomUUID();
		User reviewer = buildUser(userId, "reviewer@mail.com");
		EmailCampaign campaign = buildCampaign(campaignId, buildUser(UUID.randomUUID(), "editor@mail.com"));
		CreateEmailCampaignReviewDTO dto = new CreateEmailCampaignReviewDTO(EmailCampaignStatus.PENDING, "ajustar assunto", campaignId);

		when(userDetails.getUsername()).thenReturn("reviewer@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("reviewer@mail.com")).thenReturn(Optional.of(reviewer));
		when(emailCampaignRepository.findByIdAndIsActiveTrue(campaignId)).thenReturn(Optional.of(campaign));
		when(emailCampaignReviewRepository.findByEmailCampaignAndIsActiveTrue(campaign)).thenReturn(List.of());
		when(emailCampaignReviewRepository.save(any(EmailCampaignReview.class))).thenAnswer(invocation -> {
			EmailCampaignReview review = invocation.getArgument(0);
			review.setId(reviewId);
			return review;
		});

		EmailCampaignReviewResponseDTO response = emailCampaignReviewService.createEmailCampaignReview(dto, userDetails);

		assertEquals(reviewId, response.id());
		assertEquals(EmailCampaignStatus.PENDING, response.status());
		assertEquals("ajustar assunto", response.comment());
	}

	@Test
	void createEmailCampaignReviewShouldThrowWhenAlreadyHasTwoReviews() {
		UUID campaignId = UUID.randomUUID();
		User reviewer = buildUser(UUID.randomUUID(), "reviewer@mail.com");
		EmailCampaign campaign = buildCampaign(campaignId, buildUser(UUID.randomUUID(), "editor@mail.com"));
		CreateEmailCampaignReviewDTO dto = new CreateEmailCampaignReviewDTO(EmailCampaignStatus.PENDING, "comentario", campaignId);

		when(userDetails.getUsername()).thenReturn("reviewer@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("reviewer@mail.com")).thenReturn(Optional.of(reviewer));
		when(emailCampaignRepository.findByIdAndIsActiveTrue(campaignId)).thenReturn(Optional.of(campaign));
		when(emailCampaignReviewRepository.findByEmailCampaignAndIsActiveTrue(campaign))
				.thenReturn(List.of(new EmailCampaignReview(), new EmailCampaignReview()));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> emailCampaignReviewService.createEmailCampaignReview(dto, userDetails));

		assertEquals("The pairs of evaluators for this campaign have already been defined.", ex.getMessage());
		verify(emailCampaignReviewRepository, never()).save(any(EmailCampaignReview.class));
	}

	@Test
	void updateEmailCampaignReviewShouldThrowWhenCampaignAlreadyAvailable() {
		UUID reviewId = UUID.randomUUID();
		User reviewer = buildUser(UUID.randomUUID(), "reviewer@mail.com");
		EmailCampaign campaign = buildCampaign(UUID.randomUUID(), buildUser(UUID.randomUUID(), "editor@mail.com"));
		campaign.setAvailable(true);
		EmailCampaignReview review = buildReview(reviewId, campaign, reviewer, EmailCampaignStatus.PENDING);

		when(userDetails.getUsername()).thenReturn("reviewer@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("reviewer@mail.com")).thenReturn(Optional.of(reviewer));
		when(emailCampaignReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> emailCampaignReviewService.updateEmailCampaignReview(reviewId,
						new UpdateEmailCampaignReviewDTO(EmailCampaignStatus.APPROVED, "ok"), userDetails));

		assertEquals("You can not update this review, this email campaign is already available.", ex.getMessage());
	}

	@Test
	void updateEmailCampaignReviewShouldThrowWhenReviewerHasNoPermission() {
		UUID reviewId = UUID.randomUUID();
		User loggedUser = buildUser(UUID.randomUUID(), "reviewer@mail.com");
		User actualReviewer = buildUser(UUID.randomUUID(), "other@mail.com");
		EmailCampaign campaign = buildCampaign(UUID.randomUUID(), buildUser(UUID.randomUUID(), "editor@mail.com"));
		EmailCampaignReview review = buildReview(reviewId, campaign, actualReviewer, EmailCampaignStatus.PENDING);

		when(userDetails.getUsername()).thenReturn("reviewer@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("reviewer@mail.com")).thenReturn(Optional.of(loggedUser));
		when(emailCampaignReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> emailCampaignReviewService.updateEmailCampaignReview(reviewId,
						new UpdateEmailCampaignReviewDTO(EmailCampaignStatus.APPROVED, "ok"), userDetails));

		assertEquals("You do not have permission to update this review.", ex.getMessage());
	}

	@Test
	void listEmailCampaignReviewShouldReturnWhenFound() {
		UUID reviewId = UUID.randomUUID();
		EmailCampaignReview review = buildReview(reviewId,
				buildCampaign(UUID.randomUUID(), buildUser(UUID.randomUUID(), "editor@mail.com")),
				buildUser(UUID.randomUUID(), "reviewer@mail.com"),
				EmailCampaignStatus.PENDING);

		when(emailCampaignReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		EmailCampaignReviewResponseDTO response = emailCampaignReviewService.listEmailCampaignReview(reviewId);

		assertEquals(reviewId, response.id());
		assertEquals(EmailCampaignStatus.PENDING, response.status());
	}

	@Test
	void listEmailCampaignReviewsShouldReturnReviewsFromReviewer() {
		User reviewer = buildUser(UUID.randomUUID(), "reviewer@mail.com");
		EmailCampaign campaign = buildCampaign(UUID.randomUUID(), buildUser(UUID.randomUUID(), "editor@mail.com"));
		EmailCampaignReview review = buildReview(UUID.randomUUID(), campaign, reviewer, EmailCampaignStatus.PENDING);

		when(userDetails.getUsername()).thenReturn("reviewer@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("reviewer@mail.com")).thenReturn(Optional.of(reviewer));
		when(emailCampaignReviewRepository.findByReviewerAndIsActiveTrue(reviewer)).thenReturn(List.of(review));

		List<EmailCampaignReviewResponseDTO> response = emailCampaignReviewService.listEmailCampaignReviews(userDetails);

		assertEquals(1, response.size());
		assertEquals(review.getId(), response.get(0).id());
	}

	@Test
	void deleteEmailCampaignReviewShouldDisableReviewWhenAllowed() {
		UUID reviewId = UUID.randomUUID();
		User reviewer = buildUser(UUID.randomUUID(), "reviewer@mail.com");
		EmailCampaign campaign = buildCampaign(UUID.randomUUID(), buildUser(UUID.randomUUID(), "editor@mail.com"));
		EmailCampaignReview review = buildReview(reviewId, campaign, reviewer, EmailCampaignStatus.PENDING);

		when(userDetails.getUsername()).thenReturn("reviewer@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("reviewer@mail.com")).thenReturn(Optional.of(reviewer));
		when(emailCampaignReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		emailCampaignReviewService.deleteEmailCampaignReview(reviewId, userDetails);

		verify(emailCampaignReviewRepository).setEmailCampaignReviewAsNonActive(review);
	}

	@Test
	void cascadeDeleteEmailCampaignsShouldDisableAllReviewsByEditor() {
		UUID campaignId = UUID.randomUUID();
		UUID editorId = UUID.randomUUID();
		User editor = buildUser(editorId, "editor@mail.com");
		EmailCampaign campaign = buildCampaign(campaignId, editor);
		EmailCampaignReview review1 = buildReview(UUID.randomUUID(), campaign, buildUser(UUID.randomUUID(), "r1@mail.com"), EmailCampaignStatus.PENDING);
		EmailCampaignReview review2 = buildReview(UUID.randomUUID(), campaign, buildUser(UUID.randomUUID(), "r2@mail.com"), EmailCampaignStatus.PENDING);

		when(emailCampaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
		when(emailCampaignReviewRepository.findAllByUserIdAndIsActiveTrue(editorId)).thenReturn(List.of(review1, review2));

		emailCampaignReviewService.cascadeDeleteEmailCampaigns(new EmailCampaignDeletedEvent(campaignId));

		verify(emailCampaignReviewRepository).setEmailCampaignReviewAsNonActive(review1);
		verify(emailCampaignReviewRepository).setEmailCampaignReviewAsNonActive(review2);
	}

	private User buildUser(UUID id, String email) {
		User user = new User();
		user.setId(id);
		user.setName("Kio");
		user.setEmail(email);
		user.setPassword("encoded");
		user.setRole(Role.REVIEWER);
		user.setActive(true);
		return user;
	}

	private EmailCampaign buildCampaign(UUID id, User editor) {
		EmailContent content = new EmailContent();
		content.setId(UUID.randomUUID());
		content.setTopic("TECH");
		content.setSubject("subject");
		content.setBody("body");
		content.setEditor(editor);
		content.setCreatedAt(LocalDateTime.now());
		content.setActive(true);

		EmailCampaign campaign = new EmailCampaign();
		campaign.setId(id);
		campaign.setTextPreview("preview");
		campaign.setEmailContent(content);
		campaign.setAvailable(false);
		campaign.setCreatedAt(LocalDateTime.now());
		return campaign;
	}

	private EmailCampaignReview buildReview(UUID id, EmailCampaign campaign, User reviewer, EmailCampaignStatus status) {
		EmailCampaignReview review = new EmailCampaignReview();
		review.setId(id);
		review.setStatus(status);
		review.setComment("comment");
		review.setEmailCampaign(campaign);
		review.setReviewer(reviewer);
		review.setActive(true);
		review.setCreatedAt(LocalDateTime.now());
		return review;
	}
}
