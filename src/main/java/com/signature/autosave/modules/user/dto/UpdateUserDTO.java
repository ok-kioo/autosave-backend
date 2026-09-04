package com.signature.autosave.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserDTO(String name,

                            @Email
                            String email,

                            @Size(min = 8)
                            String password) {

}
