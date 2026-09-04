package com.signature.autosave.modules.user.dto;

import com.signature.autosave.modules.user.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UserResponseDTO(@NotBlank UUID id, @NotBlank String name, @NotBlank @Email String email, Role role) {

}
