package com.signature.autosave.modules.user.dto;

import com.signature.autosave.modules.user.domain.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Getter
public class UpdateRoleUserDTO {
    @Enumerated(EnumType.STRING)
    private Role role;
}
