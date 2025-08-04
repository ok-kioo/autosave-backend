package com.signature.autosave.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateUserDTO {
    private String username;

    @Email
    private String email;

    @Size(min = 8)
    private String password;
}
