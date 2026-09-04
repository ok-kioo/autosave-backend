package com.signature.autosave.modules.auth.dto;

import com.signature.autosave.modules.user.dto.UserResponseDTO;
import jakarta.validation.constraints.NotNull;

public record AuthResponseDTO(@NotNull String token, @NotNull UserResponseDTO user) {
}


