package com.signature.autosave.services.email;

import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaignReview;
import com.signature.autosave.modules.email.campaign.domain.repository.EmailCampaignReviewRepository;
import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.email.content.domain.enums.EmailTopic;
import com.signature.autosave.modules.email.content.domain.repository.EmailContentRepository;
import com.signature.autosave.modules.email.content.dto.CreateEmailContentDTO;
import com.signature.autosave.modules.email.content.dto.EmailContentResponseDTO;
import com.signature.autosave.modules.email.content.dto.UpdateEmailContentDTO;
import com.signature.autosave.modules.email.content.service.EmailContentService;
import com.signature.autosave.modules.email.content.service.events.EmailContentDeletedEvent;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.enums.Role;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import com.signature.autosave.modules.user.service.events.UserDeletedEvent;
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
public class EmailContentServiceTest {
	@Mock
	private EmailContentRepository emailContentRepository;
	@Mock
	private EmailCampaignReviewRepository emailCampaignReviewRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private UserDetails userDetails;
	@InjectMocks
	private EmailContentService emailContentService;

	@Test
	void createEmailContentShouldSaveAndReturnResponse() {
		UUID userId = UUID.randomUUID();
		UUID contentId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		CreateEmailContentDTO dto = new CreateEmailContentDTO("Titulo", EmailTopic.TECH, "Corpo");

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(emailContentRepository.save(any(EmailContent.class))).thenAnswer(invocation -> {
			EmailContent content = invocation.getArgument(0);
			content.setId(contentId);
			return content;
		});

		EmailContentResponseDTO response = emailContentService.createEmailContent(dto, userDetails);

		assertEquals(contentId, response.id());
		assertEquals("TECH", response.topic());
		assertEquals("Titulo", response.subject());
		assertEquals("Corpo", response.body());
		assertEquals(userId, response.editor().getId());
		verify(emailContentRepository).save(any(EmailContent.class));
	}

	@Test
	void listEmailContentShouldReturnWhenFound() {
		UUID id = UUID.randomUUID();
		EmailContent content = buildEmailContent(id, buildUser(UUID.randomUUID(), "editor@mail.com"), "TECH", "S", "B");

		when(emailContentRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.of(content));

		EmailContentResponseDTO response = emailContentService.listEmailContent(id);

		assertEquals(id, response.id());
		assertEquals("TECH", response.topic());
	}

	@Test
	void listEmailContentsShouldReturnMappedPage() {
		UUID userId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		Pageable pageable = PageRequest.of(0, 10);
		EmailContent content = buildEmailContent(UUID.randomUUID(), user, "TECH", "Titulo", "Corpo");
		Page<EmailContent> page = new PageImpl<>(List.of(content), pageable, 1);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(emailContentRepository.findByEmailContentEditorAndIsActiveTrue(userId, "tech", pageable)).thenReturn(page);

		Page<EmailContentResponseDTO> response = emailContentService.listEmailContents(userDetails, pageable, "tech");

		assertEquals(1, response.getTotalElements());
		assertEquals("Titulo", response.getContent().get(0).subject());
	}

	@Test
	void updateEmailContentShouldThrowWhenUserHasNoPermission() {
		UUID userId = UUID.randomUUID();
		UUID id = UUID.randomUUID();
		User loggedUser = buildUser(userId, "kio@mail.com");
		User editor = buildUser(UUID.randomUUID(), "editor@mail.com");
		EmailContent content = buildEmailContent(id, editor, "TECH", "Old", "Body");

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(loggedUser));
		when(emailContentRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.of(content));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> emailContentService.updateEmailContent(id, new UpdateEmailContentDTO("New", EmailTopic.GAMES, "B"), userDetails));

		assertEquals("You do not have permission to update this email content.", ex.getMessage());
		verify(emailContentRepository, never()).save(any(EmailContent.class));
	}

	@Test
	void updateEmailContentShouldThrowWhenNoPendingReview() {
		UUID userId = UUID.randomUUID();
		UUID id = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		EmailContent content = buildEmailContent(id, user, "TECH", "Old", "Body");

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(emailContentRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.of(content));
		when(emailCampaignReviewRepository.findByEmailContent(content)).thenReturn(List.of(new EmailCampaignReview()));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> emailContentService.updateEmailContent(id, new UpdateEmailContentDTO("New", EmailTopic.GAMES, "B"), userDetails));

		assertEquals("The content may only be edited if requested by an reviewer.", ex.getMessage());
		verify(emailContentRepository, never()).save(any(EmailContent.class));
	}

	@Test
	void deleteEmailContentShouldDisableAndPublishEvent() {
		UUID userId = UUID.randomUUID();
		UUID id = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		EmailContent content = buildEmailContent(id, user, "TECH", "Titulo", "Body");

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(emailContentRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.of(content));

		emailContentService.deleteEmailContent(id, userDetails);

		verify(emailContentRepository).setEmailContentAsNonActive(content);
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(publisher).publishEvent(captor.capture());
		EmailContentDeletedEvent event = assertInstanceOf(EmailContentDeletedEvent.class, captor.getValue());
		assertEquals(id, event.emailContentId());
	}

	@Test
	void cascadeDeleteEmailContentsShouldDisableAllAndPublishEvents() {
		UUID userId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		EmailContent content1 = buildEmailContent(UUID.randomUUID(), user, "TECH", "A", "Body");
		EmailContent content2 = buildEmailContent(UUID.randomUUID(), user, "GAMES", "B", "Body");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(emailContentRepository.findAllByUserIdAndIsActiveTrue(userId)).thenReturn(List.of(content1, content2));

		emailContentService.cascadeDeleteEmailContents(new UserDeletedEvent(userId));

		verify(emailContentRepository).setEmailContentAsNonActive(content1);
		verify(emailContentRepository).setEmailContentAsNonActive(content2);
		verify(publisher, org.mockito.Mockito.times(2)).publishEvent(any(EmailContentDeletedEvent.class));
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

	private EmailContent buildEmailContent(UUID id, User editor, String topic, String subject, String body) {
		EmailContent content = new EmailContent();
		content.setId(id);
		content.setTopic(topic);
		content.setSubject(subject);
		content.setBody(body);
		content.setEditor(editor);
		content.setCreatedAt(LocalDateTime.now());
		content.setActive(true);
		return content;
	}
}
