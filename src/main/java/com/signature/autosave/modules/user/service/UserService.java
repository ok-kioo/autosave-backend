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
        userRepository.findByEmail(registerDTO.email()).ifPresent(user -> {
            throw new IllegalArgumentException("Email já cadastrado");
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public UserResponseDTO updateUser(UpdateUserDTO updateUserDTO, UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        user.setRole(updateRoleUserDTO.role());
        userRepository.save(user);

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    @Transactional
    public void deleteUser(DeleteUserDTO deleteUserDTO, UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!user.getId().equals(id)) {
            throw new IllegalArgumentException("Id do usuário não corresponde ao usuário autenticado");
        }

        if(!user.getPassword().equals(deleteUserDTO.password())){
            throw new IllegalArgumentException("Senha incorreta");
        }

        userRepository.delete(user);

        publisher.publishEvent(new UserDeletedEvent(user.getId()));
    }
}
