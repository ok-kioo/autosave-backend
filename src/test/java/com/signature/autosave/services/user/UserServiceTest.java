package com.signature.autosave.services.user;

import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.enums.Role;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import com.signature.autosave.modules.user.dto.DeleteUserDTO;
import com.signature.autosave.modules.user.dto.RegisterDTO;
import com.signature.autosave.modules.user.dto.UpdateRoleUserDTO;
import com.signature.autosave.modules.user.dto.UpdateUserDTO;
import com.signature.autosave.modules.user.dto.UserResponseDTO;
import com.signature.autosave.modules.user.service.UserService;
import com.signature.autosave.modules.user.service.events.UserCreatedEvent;
import com.signature.autosave.modules.user.service.events.UserDeletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private UserDetails userDetails;
    @InjectMocks
    private UserService userService;

    @Test
    void createUserShouldSaveUserAndPublishEvent() {
        RegisterDTO dto = new RegisterDTO("Kio", "kio@mail.com", "12345678");
        UUID generatedId = UUID.randomUUID();

        when(userRepository.findByEmailAndIsActiveTrue(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.password())).thenReturn("encoded-password");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(generatedId);
            return user;
        }).when(userRepository).save(any(User.class));

        UserResponseDTO response = userService.createUser(dto);

        assertEquals(generatedId, response.id());
        assertEquals("Kio", response.name());
        assertEquals("kio@mail.com", response.email());
        assertEquals(Role.VIEWER, response.role());

        verify(userRepository).save(any(User.class));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        UserCreatedEvent event = assertInstanceOf(UserCreatedEvent.class, eventCaptor.getValue());
        assertEquals(generatedId, event.user());
    }

    @Test
    void createUserShouldThrowWhenEmailAlreadyExists() {
        RegisterDTO dto = new RegisterDTO("Kio", "kio@mail.com", "12345678");
        User existingUser = buildUser(UUID.randomUUID(), "Another", dto.email(), "pwd", Role.VIEWER);

        when(userRepository.findByEmailAndIsActiveTrue(dto.email())).thenReturn(Optional.of(existingUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(dto));

        assertEquals("Email already registered.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void listShouldReturnUserWhenFound() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "Kio", "kio@mail.com", "encoded", Role.EDITOR);

        when(userRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.list(id);

        assertEquals(id, response.id());
        assertEquals("Kio", response.name());
        assertEquals("kio@mail.com", response.email());
        assertEquals(Role.EDITOR, response.role());
    }

    @Test
    void listShouldThrowWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.list(id));

        assertEquals("User not found.", ex.getMessage());
    }

    @Test
    void updateUserShouldUpdateFieldsAndEncodePassword() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "Old Name", "old@mail.com", "old-pass", Role.VIEWER);
        UpdateUserDTO dto = new UpdateUserDTO("New Name", "new@mail.com", "new-password");

        when(userDetails.getUsername()).thenReturn("old@mail.com");
        when(userRepository.findByEmailAndIsActiveTrue("old@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        UserResponseDTO response = userService.updateUser(dto, id, userDetails);

        assertEquals(id, response.id());
        assertEquals("New Name", response.name());
        assertEquals("new@mail.com", response.email());
        assertEquals(Role.VIEWER, response.role());
        assertEquals("encoded-new-password", user.getPassword());

        verify(userRepository).save(user);
    }

    @Test
    void updateUserShouldThrowWhenAuthenticatedUserNotFound() {
        UpdateUserDTO dto = new UpdateUserDTO("Name", "mail@mail.com", "12345678");

        when(userDetails.getUsername()).thenReturn("notfound@mail.com");
        when(userRepository.findByEmailAndIsActiveTrue("notfound@mail.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(dto, UUID.randomUUID(), userDetails));

        assertEquals("User not found.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateRoleUserShouldUpdateRole() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "Kio", "kio@mail.com", "pwd", Role.VIEWER);
        UpdateRoleUserDTO dto = new UpdateRoleUserDTO(Role.ADMIN);

        when(userRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.updateRoleUser(dto, id);

        assertEquals(Role.ADMIN, response.role());
        assertEquals(Role.ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void updateRoleUserShouldThrowWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateRoleUser(new UpdateRoleUserDTO(Role.ADMIN), id));

        assertEquals("User not found.", ex.getMessage());
    }

    @Test
    void deleteUserShouldDisableUserAndPublishEvent() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "Kio", "kio@mail.com", "12345678", Role.VIEWER);

        when(userDetails.getUsername()).thenReturn("kio@mail.com");
        when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));

        userService.deleteUser(new DeleteUserDTO("12345678"), id, userDetails);

        verify(userRepository).setUserAsNonActive(user);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        UserDeletedEvent event = assertInstanceOf(UserDeletedEvent.class, eventCaptor.getValue());
        assertEquals(id, event.userId());
    }

    @Test
    void deleteUserShouldThrowWhenIdDoesNotMatchAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        User user = buildUser(userId, "Kio", "kio@mail.com", "12345678", Role.VIEWER);

        when(userDetails.getUsername()).thenReturn("kio@mail.com");
        when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(new DeleteUserDTO("12345678"), requestId, userDetails));

        assertEquals("User Id does not match the authenticated user.", ex.getMessage());
        verify(userRepository, never()).setUserAsNonActive(any(User.class));
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void deleteUserShouldThrowWhenPasswordIsWrong() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "Kio", "kio@mail.com", "12345678", Role.VIEWER);

        when(userDetails.getUsername()).thenReturn("kio@mail.com");
        when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(new DeleteUserDTO("wrongpass"), id, userDetails));

        assertEquals("Wrong password.", ex.getMessage());
        verify(userRepository, never()).setUserAsNonActive(any(User.class));
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void deleteUserShouldThrowWhenAuthenticatedUserNotFound() {
        when(userDetails.getUsername()).thenReturn("missing@mail.com");
        when(userRepository.findByEmailAndIsActiveTrue("missing@mail.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(new DeleteUserDTO("12345678"), UUID.randomUUID(), userDetails));

        assertEquals("User not found.", ex.getMessage());
        verify(userRepository, never()).setUserAsNonActive(any(User.class));
        verify(publisher, never()).publishEvent(any());
    }

    private User buildUser(UUID id, String name, String email, String password, Role role) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setActive(true);
        return user;
    }

}