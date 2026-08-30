package com.signature.autosave.modules.user.service;

import com.signature.autosave.modules.user.builder.UserBuilder;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import com.signature.autosave.modules.user.dto.*;
import com.signature.autosave.modules.user.service.events.UserCreatedEvent;
import com.signature.autosave.modules.user.service.events.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public UserResponseDTO createUser(RegisterDTO registerDTO){
        userRepository.findByEmailAndIsActiveTrue(registerDTO.email()).ifPresent(user -> {
            throw new IllegalArgumentException("Email already registered.");
        });

        String encodedPassword = passwordEncoder.encode(registerDTO.password());

        User user = UserBuilder.builder()
                    .withName(registerDTO.name())
                    .withEmail(registerDTO.email())
                    .withPassword(encodedPassword)
                    .build();

        userRepository.save(user);

        publisher.publishEvent(new UserCreatedEvent(user.getId()));

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO list(UUID id){
        User user = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public UserResponseDTO updateUser(UpdateUserDTO updateUserDTO, UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Optional.ofNullable(updateUserDTO.email())
                .ifPresent(user::setEmail);

        Optional.ofNullable(updateUserDTO.name())
                .ifPresent(user::setName);

        Optional.ofNullable(updateUserDTO.password())
                .ifPresent(password -> user.setPassword(passwordEncoder.encode(password)));

        userRepository.save(user);

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public UserResponseDTO updateRoleUser(UpdateRoleUserDTO updateRoleUserDTO, UUID id) {
        User user = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        user.setRole(updateRoleUserDTO.role());
        userRepository.save(user);

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    @Transactional
    public void deleteUser(DeleteUserDTO deleteUserDTO, UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!user.getId().equals(id)) {
            throw new IllegalArgumentException("User Id does not match the authenticated user.");
        }

        if(!user.getPassword().equals(deleteUserDTO.password())){
            throw new IllegalArgumentException("Wrong password.");
        }

        userRepository.setUserAsNonActive(user);

        publisher.publishEvent(new UserDeletedEvent(user.getId()));
    }
}
