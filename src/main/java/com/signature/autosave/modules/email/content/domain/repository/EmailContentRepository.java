package com.signature.autosave.modules.email.content.domain.repository;

import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailContentRepository extends JpaRepository<EmailContent, UUID> {
    Optional<EmailContent>findByIdAndIsActiveTrue(UUID id);

    List<EmailContent> findByEmailContentEditorAndIsActiveTrue(User user);

    @Modifying
    @Transactional
    @Query("""
    UPDATE EmailContent ec
    SET ec.isActive = false,
        ec.disabledAt = CURRENT_TIMESTAMP
    WHERE ec = :emailContent
""")
    void setEmailContentAsNonActive(@Param("emailContent") EmailContent emailContent);


}
