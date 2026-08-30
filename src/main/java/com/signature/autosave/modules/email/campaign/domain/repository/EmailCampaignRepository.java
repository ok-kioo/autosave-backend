package com.signature.autosave.modules.email.campaign.domain.repository;

import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, UUID> {
    Optional<EmailCampaign> findByIdAndIsActiveTrue(UUID id);

    @Query("""
    SELECT ec
    FROM EmailCampaign ec
    WHERE ec.emailContent.editor.id = :userId
      AND ec.isActive = true
""")
    List<EmailCampaign> findAllByUserIdAndIsActiveTrue(
            @Param("userId") UUID userId
    );

    @Query(
            value = """
        SELECT ec.*
        FROM email_campaign ec
        JOIN email_content ect
            ON ect.id = ec.email_content_id
        WHERE ect.editor_id = :userId
          AND ec.is_available = true
          AND ect.is_active = true
          AND (
              :searchTerm IS NULL
              OR to_tsvector('portuguese', ec.text_preview)
                 @@ plainto_tsquery('portuguese', :searchTerm)
          )
        """,
            countQuery = """
        SELECT COUNT(ec.id)
        FROM email_campaign ec
        JOIN email_content ect
            ON ect.id = ec.email_content_id
        WHERE ect.editor_id = :userId
          AND ec.is_available = true
          AND ect.is_active = true
          AND (
              :searchTerm IS NULL
              OR to_tsvector('portuguese', ec.text_preview)
                 @@ plainto_tsquery('portuguese', :searchTerm)
          )
        """,
            nativeQuery = true
    )
    Page<EmailCampaign> findByEmailContentEditorAndIsActiveTrue(
            @Param("userId") UUID userId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

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

    @Query(
            value = """
        SELECT DISTINCT ec.*
        FROM email_campaign ec
        JOIN email_campaign_subscription_plan ecsp
            ON ecsp.email_campaign_id = ec.id
        JOIN subscription_plan sp
            ON sp.id = ecsp.subscription_plan_id
        WHERE sp.id = :subscriptionPlanId
          AND ec.is_available = true
          AND sp.is_active = true
          AND (
              :searchTerm IS NULL
              OR to_tsvector('portuguese', ec.text_preview)
                 @@ plainto_tsquery('portuguese', :searchTerm)
          )
        """,
            countQuery = """
        SELECT COUNT(DISTINCT ec.id)
        FROM email_campaign ec
        JOIN email_campaign_subscription_plan ecsp
            ON ecsp.email_campaign_id = ec.id
        JOIN subscription_plan sp
            ON sp.id = ecsp.subscription_plan_id
        WHERE sp.id = :subscriptionPlanId
          AND ec.is_available = true
          AND sp.is_active = true
          AND (
              :searchTerm IS NULL
              OR to_tsvector('portuguese', ec.text_preview)
                 @@ plainto_tsquery('portuguese', :searchTerm)
          )
        """,
            nativeQuery = true
    )
    Page<EmailCampaign> findAccessibleCampaigns(
            @Param("subscriptionPlanId") UUID subscriptionPlanId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
}
