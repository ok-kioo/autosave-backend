package com.signature.autosave.services.email;

import com.signature.autosave.modules.email.campaign.domain.entity.node.CampaignNode;
import com.signature.autosave.modules.email.campaign.domain.entity.node.CommentNode;
import com.signature.autosave.modules.email.campaign.domain.repository.EmailCampaignRepository;
import com.signature.autosave.modules.email.campaign.domain.repository.node.CampaignNodeRepository;
import com.signature.autosave.modules.email.campaign.domain.repository.node.CommentNodeRepository;
import com.signature.autosave.modules.email.campaign.dto.node.CommentThreadProjection;
import com.signature.autosave.modules.email.campaign.dto.node.RegisterCampaignCommentDTO;
import com.signature.autosave.modules.email.campaign.dto.node.RegisterCampaignNodeViewDTO;
import com.signature.autosave.modules.email.campaign.dto.node.RegisterReplyCommentDTO;
import com.signature.autosave.modules.email.campaign.dto.node.ToggleLikeCampaignNodeDTO;
import com.signature.autosave.modules.email.campaign.service.CampaignNodeService;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignCreatedEvent;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignDeletedEvent;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.entity.node.UserNode;
import com.signature.autosave.modules.user.domain.enums.Role;
import com.signature.autosave.modules.user.domain.repository.UserNodeRepository;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CampaignNodeServiceTest {
	@Mock
	private EmailCampaignRepository emailCampaignRepository;
	@Mock
	private CampaignNodeRepository campaignNodeRepository;
	@Mock
	private UserNodeRepository userNodeRepository;
	@Mock
	private CommentNodeRepository commentNodeRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private UserDetails userDetails;
	@InjectMocks
	private CampaignNodeService campaignNodeService;

	@Test
	void registerCampaignShouldCreateNodeWhenCampaignExists() {
		UUID campaignId = UUID.randomUUID();
		when(emailCampaignRepository.findByIdAndIsActiveTrue(campaignId)).thenReturn(Optional.of(org.mockito.Mockito.mock(com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign.class)));

		campaignNodeService.registerCampaign(new EmailCampaignCreatedEvent(campaignId));

		ArgumentCaptor<CampaignNode> captor = ArgumentCaptor.forClass(CampaignNode.class);
		verify(campaignNodeRepository).save(captor.capture());
		assertEquals(campaignId, captor.getValue().getId());
		assertEquals(true, captor.getValue().isActive());
	}

	@Test
	void deleteCampaignShouldSoftDeleteCampaignAndComments() {
		UUID campaignId = UUID.randomUUID();
		when(campaignNodeRepository.findById(campaignId)).thenReturn(Optional.of(new CampaignNode()));

		campaignNodeService.deleteCampaign(new EmailCampaignDeletedEvent(campaignId));

		verify(commentNodeRepository).softDeleteCampaignComments(campaignId);
		verify(campaignNodeRepository).softDelete(campaignId);
	}

	@Test
	void registerViewShouldRegisterWhenAuthenticatedUserMatches() {
		UUID userId = UUID.randomUUID();
		UUID campaignId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(campaignNodeRepository.findActiveById(campaignId)).thenReturn(Optional.of(new CampaignNode()));
		when(userNodeRepository.findActiveById(userId)).thenReturn(Optional.of(new UserNode()));

		campaignNodeService.registerView(new RegisterCampaignNodeViewDTO(userId, campaignId), userDetails);

		verify(campaignNodeRepository).registerView(userId, campaignId);
	}

	@Test
	void registerViewShouldThrowWhenUserIdDoesNotMatchAuthenticatedUser() {
		UUID userId = UUID.randomUUID();
		UUID otherId = UUID.randomUUID();
		UUID campaignId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> campaignNodeService.registerView(new RegisterCampaignNodeViewDTO(otherId, campaignId), userDetails));

		assertEquals("User Id does not match the authenticated user.", ex.getMessage());
		verify(campaignNodeRepository, never()).registerView(any(UUID.class), any(UUID.class));
	}

	@Test
	void countViewsShouldReturnValue() {
		UUID campaignId = UUID.randomUUID();
		when(campaignNodeRepository.findActiveById(campaignId)).thenReturn(Optional.of(new CampaignNode()));
		when(campaignNodeRepository.countViews(campaignId)).thenReturn(8L);

		Long result = campaignNodeService.countViews(campaignId);

		assertEquals(8L, result);
	}

	@Test
	void toggleLikeShouldReturnRepositoryValue() {
		UUID userId = UUID.randomUUID();
		UUID campaignId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(campaignNodeRepository.findActiveById(campaignId)).thenReturn(Optional.of(new CampaignNode()));
		when(userNodeRepository.findActiveById(userId)).thenReturn(Optional.of(new UserNode()));
		when(campaignNodeRepository.toggleLike(userId, campaignId)).thenReturn(true);

		Boolean result = campaignNodeService.toggleLike(new ToggleLikeCampaignNodeDTO(userId, campaignId), userDetails);

		assertEquals(true, result);
	}

	@Test
	void registerCampaignCommentShouldCallRepositoryAndReturnProjection() {
		UUID userId = UUID.randomUUID();
		UUID campaignId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		CommentThreadProjection projection = org.mockito.Mockito.mock(CommentThreadProjection.class);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(campaignNodeRepository.findActiveById(campaignId)).thenReturn(Optional.of(new CampaignNode()));
		when(userNodeRepository.findActiveById(userId)).thenReturn(Optional.of(new UserNode()));
		when(commentNodeRepository.registerCampaignComment(any(UUID.class), any(UUID.class), any(UUID.class), any(String.class)))
				.thenReturn(projection);

		CommentThreadProjection result = campaignNodeService.registerCampaignComment(
				new RegisterCampaignCommentDTO(userId, campaignId, "primeiro comentario"), userDetails);

		assertEquals(projection, result);
	}

	@Test
	void registerReplyCommentShouldCallRepositoryAndReturnProjection() {
		UUID userId = UUID.randomUUID();
		UUID parentId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");
		CommentThreadProjection projection = org.mockito.Mockito.mock(CommentThreadProjection.class);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(commentNodeRepository.findActiveById(parentId)).thenReturn(Optional.of(new CommentNode()));
		when(userNodeRepository.findActiveById(userId)).thenReturn(Optional.of(new UserNode()));
		when(commentNodeRepository.replyComment(any(UUID.class), any(UUID.class), any(UUID.class), any(String.class)))
				.thenReturn(projection);

		CommentThreadProjection result = campaignNodeService.registerReplyComment(
				new RegisterReplyCommentDTO(userId, parentId, "resposta"), userDetails);

		assertEquals(projection, result);
	}

	@Test
	void listCommentsShouldUsePaginationOffsetAndLimit() {
		UUID campaignId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(1, 5);
		List<CommentThreadProjection> projections = List.of(org.mockito.Mockito.mock(CommentThreadProjection.class));

		when(campaignNodeRepository.findActiveById(campaignId)).thenReturn(Optional.of(new CampaignNode()));
		when(commentNodeRepository.findComments(campaignId, 5L, 5L)).thenReturn(projections);

		List<CommentThreadProjection> result = campaignNodeService.listComments(campaignId, pageable);

		assertEquals(1, result.size());
	}

	@Test
	void deleteCommentShouldSoftDeleteWhenWrittenByUser() {
		UUID userId = UUID.randomUUID();
		UUID commentId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(commentNodeRepository.findActiveById(commentId)).thenReturn(Optional.of(new CommentNode()));
		when(commentNodeRepository.commentWrittenByUser(commentId, userId)).thenReturn(true);

		campaignNodeService.deleteComment(commentId, userDetails);

		verify(commentNodeRepository).softDelete(commentId);
	}

	@Test
	void deleteCommentShouldThrowWhenNotWrittenByUser() {
		UUID userId = UUID.randomUUID();
		UUID commentId = UUID.randomUUID();
		User user = buildUser(userId, "kio@mail.com");

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(commentNodeRepository.findActiveById(commentId)).thenReturn(Optional.of(new CommentNode()));
		when(commentNodeRepository.commentWrittenByUser(commentId, userId)).thenReturn(false);

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> campaignNodeService.deleteComment(commentId, userDetails));

		assertEquals("You do not have permission to delete this comment.", ex.getMessage());
		verify(commentNodeRepository, never()).softDelete(commentId);
	}

	private User buildUser(UUID id, String email) {
		User user = new User();
		user.setId(id);
		user.setName("Kio");
		user.setEmail(email);
		user.setPassword("encoded");
		user.setRole(Role.VIEWER);
		user.setActive(true);
		return user;
	}


}