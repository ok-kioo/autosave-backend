package com.signature.autosave.service;

import com.signature.autosave.dto.user.*;
import com.signature.autosave.persistence.entity.User;
import com.signature.autosave.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponseDTO createUser(RegisterDTO newUser){
        userRepository.findByEmail(newUser.getEmail()).ifPresent(user -> {
            throw new IllegalArgumentException("Email already exists");
        });

        String encodedPassword = passwordEncoder.encode(newUser.getPassword());

        User user = new User();
        user.setName(newUser.getName());
        user.setEmail(newUser.getEmail());
        user.setPassword(encodedPassword);

        userRepository.save(user);

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }

    public List<UserResponseDTO> listUser(ListUserDTO users){
        List<UserResponseDTO> userResponses;

        if(users.getNames() == null){
            userResponses = userRepository.findAll().stream()
                    .map(user -> new UserResponseDTO(user.getId(), user.getName(), user.getEmail()))
                    .toList();
        } else{
            userResponses = userRepository.findUsersByNameIn(users.getNames()).stream()
                    .map(user -> new UserResponseDTO(user.getId(), user.getName(), user.getEmail()))
                    .toList();
        }

        return userResponses;
    }

    public UserResponseDTO updateUser(UpdateUserDTO updatedUser, UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getId().equals(id)) {
            throw new IllegalArgumentException("User ID does not match the authenticated user");
        }

        Optional.ofNullable(updatedUser.getEmail())
                .ifPresent(user::setEmail);

        Optional.ofNullable(updatedUser.getUsername())
                .ifPresent(user::setName);

        Optional.ofNullable(updatedUser.getPassword())
                .ifPresent(password -> user.setPassword(passwordEncoder.encode(password)));

        userRepository.save(user);

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }

    public void deleteUser(DeleteUserDTO deleteUser, UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getId().equals(id)) {
            throw new IllegalArgumentException("User ID does not match the authenticated user");
        }

        if(!user.getPassword().equals(deleteUser.getPassword())){
            throw new IllegalArgumentException("Password does not match");
        }

        userRepository.delete(user);
    }
}
