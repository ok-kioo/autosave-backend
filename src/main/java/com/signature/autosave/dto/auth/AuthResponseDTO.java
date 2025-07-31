package com.signature.autosave.dto.auth;

import com.signature.autosave.dto.user.UserResponseDTO;
import lombok.Getter;

@Getter
public class AuthResponseDTO {
    private final String token;
    private final UserResponseDTO user;

    public AuthResponseDTO(String token, UserResponseDTO user) {
        this.token = token;
        this.user = user;
    }
}


