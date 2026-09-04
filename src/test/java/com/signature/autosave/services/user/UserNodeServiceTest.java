package com.signature.autosave.services.user;

import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.entity.node.UserNode;
import com.signature.autosave.modules.user.domain.enums.Role;
import com.signature.autosave.modules.user.domain.repository.UserNodeRepository;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import com.signature.autosave.modules.user.service.UserNodeService;
import com.signature.autosave.modules.user.service.events.UserCreatedEvent;
import com.signature.autosave.modules.user.service.events.UserDeletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserNodeServiceTest {
	@Mock
	private UserRepository userRepository;
	@Mock
	private UserNodeRepository userNodeRepository;
	@InjectMocks
	private UserNodeService userNodeService;

	@Test
	void registerUserNodeShouldCreateAndSaveNode() {
		UUID userId = UUID.randomUUID();
		User user = new User();
		user.setId(userId);
		user.setName("Kio");
		user.setEmail("kio@mail.com");
		user.setPassword("encoded");
		user.setRole(Role.VIEWER);
		user.setActive(true);

		when(userRepository.findByIdAndIsActiveTrue(userId)).thenReturn(Optional.of(user));

		userNodeService.registerUserNode(new UserCreatedEvent(userId));

		ArgumentCaptor<UserNode> captor = ArgumentCaptor.forClass(UserNode.class);
		verify(userNodeRepository).save(captor.capture());
		assertEquals(userId, captor.getValue().getId());
		assertEquals("Kio", captor.getValue().getName());
		assertEquals(true, captor.getValue().isActive());
	}

	@Test
	void registerUserNodeShouldThrowWhenUserNotFound() {
		UUID userId = UUID.randomUUID();
		when(userRepository.findByIdAndIsActiveTrue(userId)).thenReturn(Optional.empty());

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> userNodeService.registerUserNode(new UserCreatedEvent(userId)));

		assertEquals("User not found", ex.getMessage());
		verify(userNodeRepository, never()).save(org.mockito.ArgumentMatchers.any(UserNode.class));
	}

	@Test
	void deleteUserNodeShouldSoftDeleteWhenNodeExists() {
		UUID userId = UUID.randomUUID();
		when(userNodeRepository.findActiveById(userId)).thenReturn(Optional.of(new UserNode()));

		userNodeService.deleteUserNode(new UserDeletedEvent(userId));

		verify(userNodeRepository).softDelete(userId);
	}

	@Test
	void deleteUserNodeShouldThrowWhenNodeNotFound() {
		UUID userId = UUID.randomUUID();
		when(userNodeRepository.findActiveById(userId)).thenReturn(Optional.empty());

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> userNodeService.deleteUserNode(new UserDeletedEvent(userId)));

		assertEquals("User node not found", ex.getMessage());
		verify(userNodeRepository, never()).softDelete(userId);
	}


}