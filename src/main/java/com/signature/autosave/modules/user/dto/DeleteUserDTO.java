package com.signature.autosave.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeleteUserDTO(@NotBlank
                            @Size(min=8)
                            String password) {

}
