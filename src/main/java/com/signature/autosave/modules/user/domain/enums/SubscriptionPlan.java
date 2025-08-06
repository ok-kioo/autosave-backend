package com.signature.autosave.modules.user.domain.enums;

import lombok.Getter;

@Getter
public enum SubscriptionPlan {
    FREE("FREE"),
    BASIC("BASIC"),
    PREMIUM("PREMIUM");

    private final String planName;

    SubscriptionPlan(String planName) {
        this.planName = planName;
    }
}
