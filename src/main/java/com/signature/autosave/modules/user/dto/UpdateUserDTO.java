package com.signature.autosave.modules.user.dto;

import com.signature.autosave.modules.user.domain.enums.SubscriptionPlan;
import jakarta.persistence.Enumerated;
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

    @Enumerated(value = jakarta.persistence.EnumType.STRING)
    private SubscriptionPlan subscriptionPlan;
}
