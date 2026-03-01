package com.signature.autosave.modules.user.dto;

import com.signature.autosave.modules.user.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserResponseDTO {
    @NotBlank
    private UUID id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    private Role role;

    public UserResponseDTO(UUID id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
