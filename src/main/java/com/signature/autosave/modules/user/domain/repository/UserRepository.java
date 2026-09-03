package com.signature.autosave.modules.user.domain.repository;

import com.signature.autosave.modules.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
        Optional<User> findByIdAndIsActiveTrue(UUID id);
    Optional<User> findByEmailAndIsActiveTrue(String email);

    @Modifying
    @Transactional
    @Query("""
    UPDATE User u
    SET u.isActive = false,
        u.disabledAt = CURRENT_TIMESTAMP
    WHERE u = :user
""")
    void setUserAsNonActive(@Param("user") User user);

    @Query("""
    SELECT u.email
    FROM User u
    JOIN u.planContract pc
    JOIN pc.subscriptionPlan sp
    JOIN EmailCampaign ec
    JOIN ec.subscriptionPlans campaignPlan
    WHERE ec.id = :campaignId
      AND campaignPlan = sp
      AND ec.isAvailable = true
      AND sp.isActive = true
      AND u.isActive = true
""")
    List<String> findUsersEligibleForCampaign(@Param("campaignId") UUID campaignId);
}
