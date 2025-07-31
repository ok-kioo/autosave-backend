package com.signature.autosave.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

import lombok.Getter;

@Getter
public class UserResponseDTO {
    @NotBlank
    private UUID id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    public UserResponseDTO(UUID id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
