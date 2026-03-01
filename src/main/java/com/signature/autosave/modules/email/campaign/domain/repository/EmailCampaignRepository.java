package com.signature.autosave.modules.email.campaign.domain.repository;

import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, UUID> {
    List<EmailCampaign> findByEmailContentEditor(User user);

    @Query("""
    SELECT ec
    FROM EmailCampaign ec
    JOIN ec.subscriptionPlans sp
    WHERE sp.id = :#{#user.planContract.subscriptionPlan.id}
      AND ec.isAvailable = true
      AND sp.isActive = true
""")
    List<EmailCampaign> findAccessibleCampaignsByUser(User user);
}
