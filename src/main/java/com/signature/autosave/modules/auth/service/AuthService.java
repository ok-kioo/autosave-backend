package com.signature.autosave.modules.auth.service;

import com.signature.autosave.infra.components.jwt.IAuthComponent;
import com.signature.autosave.modules.auth.dto.AuthResponseDTO;
import com.signature.autosave.modules.auth.dto.LoginDTO;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import com.signature.autosave.modules.user.dto.UserResponseDTO;
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
    private final IAuthComponent jwtComponent;

    public AuthResponseDTO login(LoginDTO user) {
        User existingUser = userRepository.findByEmail(user.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuário com este e-mail não existe"));

        if(!passwordEncoder.matches(user.password(), existingUser.getPassword())){
            throw new IllegalArgumentException("Senha incorreta");
        }

        String token = jwtComponent.generateToken(existingUser);
        UserResponseDTO userResponse =
                new UserResponseDTO(existingUser.getId(), existingUser.getName(), existingUser.getEmail(), existingUser.getRole());

        return new AuthResponseDTO( token, userResponse);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + username));
    }
}
