package com.signature.autosave.service;

import com.signature.autosave.dto.user.ListUserDTO;
import com.signature.autosave.dto.user.RegisterDTO;
import com.signature.autosave.dto.user.UserResponseDTO;
import com.signature.autosave.persistence.entity.User;
import com.signature.autosave.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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

    /*public ArrayList<UserResponseDTO> listUsers(ListUserDTO users){


    }*/
}
