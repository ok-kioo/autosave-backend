package com.signature.autosave.modules.subscription.builder;

import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import com.signature.autosave.modules.subscription.domain.enums.BillingCycle;

import java.math.BigDecimal;

public class SubscriptionPlanBuilder {
    private String name;
    private BigDecimal price;
    private BillingCycle billingCycle;
    private String description;
    private Integer trialDays;

    private SubscriptionPlanBuilder() {
    }

    public static SubscriptionPlanBuilder builder() {
        return new SubscriptionPlanBuilder();
    }


    public SubscriptionPlanBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public SubscriptionPlanBuilder withPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public SubscriptionPlanBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public SubscriptionPlanBuilder withBillingCycle(BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
        return this;
    }

    public SubscriptionPlanBuilder withTrialDays(Integer trialDays) {
        this.trialDays = trialDays;
        return this;
    }

    public SubscriptionPlan build() {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(name);
        plan.setPrice(price);
        plan.setBillingCycle(billingCycle);
        plan.setDescription(description);
        plan.setTrialDays(trialDays);
        return plan;
    }
}
