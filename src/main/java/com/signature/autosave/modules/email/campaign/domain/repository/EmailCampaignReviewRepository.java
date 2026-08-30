package com.signature.autosave.modules.email.campaign.domain.repository;

import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaignReview;
import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EmailCampaignReviewRepository extends JpaRepository<EmailCampaignReview, UUID> {
    @Query("""
            SELECT ecr FROM EmailCampaignReview ecr
            JOIN ecr.emailCampaign ec
            ON ec.emailContent = :emailContent
            WHERE ecr.isActive = True
            """)
    List<EmailCampaignReview> findByEmailContent(@Param("emailContent") EmailContent emailContent);


    List<EmailCampaignReview> findByEmailCampaignAndIsActiveTrue(EmailCampaign emailCampaign);


    List<EmailCampaignReview> findByReviewerAndIsActiveTrue(@Param("user") User user);

}
