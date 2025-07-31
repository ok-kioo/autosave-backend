package com.signature.autosave.service;

import com.signature.autosave.components.jwt.JWTProvider;
import com.signature.autosave.dto.auth.AuthResponseDTO;
import com.signature.autosave.dto.auth.LoginDTO;
import com.signature.autosave.dto.user.UserResponseDTO;
import com.signature.autosave.persistence.entity.User;
import com.signature.autosave.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JWTProvider jwtProvider;

    public AuthResponseDTO login(LoginDTO user) {
        User existingUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuário com este e-mail não existe"));

        if(!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())){
            throw new IllegalArgumentException("Senha incorreta");
        }

        String token = jwtProvider.generateToken(existingUser);
        UserResponseDTO userResponse = new UserResponseDTO(existingUser.getId(), existingUser.getName(), existingUser.getEmail());

        return new AuthResponseDTO( token, userResponse);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + username));
    }
}
