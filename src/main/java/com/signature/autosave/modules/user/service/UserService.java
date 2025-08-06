package com.signature.autosave.modules.user.service;

import com.signature.autosave.modules.user.builder.UserBuilder;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import com.signature.autosave.modules.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponseDTO createUser(RegisterDTO registerDTO){
        userRepository.findByEmail(registerDTO.getEmail()).ifPresent(user -> {
            throw new IllegalArgumentException("Email já cadastrado");
        });

        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());

        User user = UserBuilder.builder()
                    .withNickName(registerDTO.getName())
                    .withEmail(registerDTO.getEmail())
                    .withPassword(encodedPassword)
                    .build();

        userRepository.save(user);

        return new UserResponseDTO(user.getId(), user.getNickName(), user.getEmail(), user.getSubscriptionPlan());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> list(UUID id){
        return userRepository.findById(id).stream()
                .map(user -> new UserResponseDTO(user.getId(), user.getNickName(), user.getEmail(), user.getSubscriptionPlan()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listUsersByNames(ListUserDTO listUserDTO, Pageable pageable) {
        return userRepository.findUsersByNameIn(listUserDTO.getNames(), pageable).stream()
                    .map(user -> new UserResponseDTO(user.getId(), user.getNickName(), user.getEmail(), user.getSubscriptionPlan()))
                    .toList();
    }

    public UserResponseDTO updateUser(UpdateUserDTO updateUserDTO, UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!user.getId().equals(id)) {
            throw new IllegalArgumentException("Id do usuário não corresponde ao usuário autenticado");
        }

        Optional.ofNullable(updateUserDTO.getEmail())
                .ifPresent(user::setEmail);

        Optional.ofNullable(updateUserDTO.getUsername())
                .ifPresent(user::setNickName);

        Optional.ofNullable(updateUserDTO.getPassword())
                .ifPresent(password -> user.setPassword(passwordEncoder.encode(password)));

        Optional.ofNullable(updateUserDTO.getSubscriptionPlan())
                .ifPresent(user::setSubscriptionPlan);

        userRepository.save(user);

        return new UserResponseDTO(user.getId(), user.getNickName(), user.getEmail(), user.getSubscriptionPlan());
    }

    public void deleteUser(DeleteUserDTO deleteUserDTO, UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!user.getId().equals(id)) {
            throw new IllegalArgumentException("Id do usuário não corresponde ao usuário autenticado");
        }

        if(!user.getPassword().equals(deleteUserDTO.getPassword())){
            throw new IllegalArgumentException("Senha incorreta");
        }

        userRepository.delete(user);
    }
}
