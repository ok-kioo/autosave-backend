package com.signature.autosave.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class DeleteUserDTO {
    @NotBlank
    @Size(min=8)
    private String password;
}
