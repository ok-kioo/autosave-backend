package com.signature.autosave.modules.subscription.domain.repository;

import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    Optional<SubscriptionPlan> findByIdAndIsActive(UUID uuid, boolean isActive);
    Optional<SubscriptionPlan> findByIsActive(boolean isActive);

    @Query("""
        SELECT sp
        FROM SubscriptionPlan sp
        WHERE sp.isActive = true
          AND sp.price <= :price
""")
    List<SubscriptionPlan> findEmailCampaignSubscriptionPlans(BigDecimal price);
}
