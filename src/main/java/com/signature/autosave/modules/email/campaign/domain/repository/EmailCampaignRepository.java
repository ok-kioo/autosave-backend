package com.signature.autosave.modules.email.campaign.domain.repository;

import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, UUID> {
    Optional<EmailCampaign> findByIdAndIsActiveTrue(UUID id);

    List<EmailCampaign> findByEmailContentEditorAndIsActiveTrue(User user);

    @Modifying
    @Transactional
    @Query("""
    UPDATE EmailCampaign ec
    SET ec.isActive = false,
        ec.isAvailable = false,
        ec.disabledAt = CURRENT_TIMESTAMP
    WHERE ec = :emailCampaign
""")
    void setEmailCampaignAsNonActive(@Param("emailCampaign") EmailCampaign emailCampaign);

    @Query("""
            SELECT DISTINCT ec
            FROM EmailCampaign ec
            JOIN ec.subscriptionPlans
            sp WHERE sp.id = :subscriptionPlanId
            AND ec.isAvailable = true
            AND sp.isActive = true
            """)
    List<EmailCampaign> findAccessibleCampaigns(@Param("subscriptionPlanId") UUID subscriptionPlanId);
}
