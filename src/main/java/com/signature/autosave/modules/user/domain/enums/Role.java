package com.signature.autosave.modules.user.domain.enums;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("ADMIN"),
    EDITOR("EDITOR"),
    REVIEWER("REVIEWER"),
    VIEWER("VIEWER"),
    BILLING_MANAGER("BILLING_MANAGER");

    private final String role;

    Role(String role) {
        this.role = role;
    }
}
