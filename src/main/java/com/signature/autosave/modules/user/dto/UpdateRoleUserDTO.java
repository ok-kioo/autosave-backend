package com.signature.autosave.modules.user.dto;

import com.signature.autosave.modules.user.domain.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public record UpdateRoleUserDTO(@Enumerated(EnumType.STRING)
                                Role role) {

}
