package com.signature.autosave.modules.auth.dto;

import com.signature.autosave.modules.user.dto.UserResponseDTO;
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


