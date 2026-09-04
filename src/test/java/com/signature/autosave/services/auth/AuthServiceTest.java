package com.signature.autosave.services.auth;

import com.signature.autosave.infra.components.jwt.IAuthComponent;
import com.signature.autosave.modules.auth.dto.AuthResponseDTO;
import com.signature.autosave.modules.auth.dto.LoginDTO;
import com.signature.autosave.modules.auth.service.AuthService;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.enums.Role;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private IAuthComponent jwtComponent;
    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldReturnTokenAndUserResponseWhenCredentialsAreValid() {
        LoginDTO dto = new LoginDTO("kio@mail.com", "12345678");
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "Kio", dto.email(), "encoded-password", Role.VIEWER);

        when(userRepository.findByEmailAndIsActiveTrue(dto.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(true);
        when(jwtComponent.generateToken(user)).thenReturn("jwt-token");

        AuthResponseDTO response = authService.login(dto);

        assertEquals("jwt-token", response.token());
        assertEquals(userId, response.user().id());
        assertEquals("Kio", response.user().name());
        assertEquals("kio@mail.com", response.user().email());
        assertEquals(Role.VIEWER, response.user().role());

        verify(jwtComponent).generateToken(user);
    }

    @Test
    void loginShouldThrowWhenUserDoesNotExist() {
        LoginDTO dto = new LoginDTO("missing@mail.com", "12345678");

        when(userRepository.findByEmailAndIsActiveTrue(dto.email())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(dto));

        assertEquals("Usuário com este e-mail não existe", ex.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtComponent, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void loginShouldThrowWhenPasswordIsInvalid() {
        LoginDTO dto = new LoginDTO("kio@mail.com", "wrong-pass");
        User user = buildUser(UUID.randomUUID(), "Kio", dto.email(), "encoded-password", Role.VIEWER);

        when(userRepository.findByEmailAndIsActiveTrue(dto.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(dto));

        assertEquals("Senha incorreta", ex.getMessage());
        verify(jwtComponent, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void loadUserByUsernameShouldReturnUserDetailsWhenFound() {
        String email = "kio@mail.com";
        User user = buildUser(UUID.randomUUID(), "Kio", email, "encoded-password", Role.EDITOR);

        when(userRepository.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.of(user));

        UserDetails response = authService.loadUserByUsername(email);

        User returnedUser = assertInstanceOf(User.class, response);
        assertEquals(user.getId(), returnedUser.getId());
        assertEquals(email, returnedUser.getEmail());
    }

    @Test
    void loadUserByUsernameShouldThrowWhenUserNotFound() {
        String email = "missing@mail.com";
        when(userRepository.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> authService.loadUserByUsername(email));

        assertEquals("Usuário não encontrado com email: " + email, ex.getMessage());
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