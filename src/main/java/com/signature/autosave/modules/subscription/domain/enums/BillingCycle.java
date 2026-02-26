package com.signature.autosave.modules.subscription.domain.enums;

import lombok.Getter;

@Getter
public enum BillingCycle {
    MONTHLY("months"),
    ANNUALLY("years");

    private final String cycle;

    BillingCycle(String cycle) {
        this.cycle = cycle;
    }
}
