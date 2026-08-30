package com.signature.autosave.modules.subscription.domain.repository;

import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    Optional<SubscriptionPlan> findByIdAndIsActive(UUID uuid, boolean isActive);
    Page<SubscriptionPlan> findByIsActiveTrue(Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
    UPDATE SubscriptionPlan sp
    SET sp.isActive = false,
        sp.disabledAt = CURRENT_TIMESTAMP
    WHERE sp = :subscriptionPlan
""")
    void setSubscriptionPlanAsNonActive(@Param("subscriptionPlan") SubscriptionPlan subscriptionPlan);

    @Query("""
        SELECT sp
        FROM SubscriptionPlan sp
        WHERE sp.isActive = true
          AND sp.price <= :price
""")
    List<SubscriptionPlan> findEmailCampaignSubscriptionPlans(@Param("price") BigDecimal price);
}
