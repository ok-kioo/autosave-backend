package com.signature.autosave.modules.user.domain.repository;

import com.signature.autosave.modules.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query("""
    SELECT u
    FROM User u
    JOIN u.planContract pc
    JOIN pc.subscriptionPlan sp
    JOIN EmailCampaign ec
    JOIN ec.subscriptionPlans campaignPlan
    WHERE ec.id = :campaignId
      AND campaignPlan = sp
      AND ec.isAvailable = true
      AND sp.isActive = true
""")
    List<User> findUsersEligibleForCampaign(@Param("campaignId") UUID campaignId);
}
