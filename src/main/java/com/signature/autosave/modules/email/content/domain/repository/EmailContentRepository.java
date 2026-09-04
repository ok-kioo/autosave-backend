package com.signature.autosave.modules.email.content.domain.repository;

import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(
            value = """
        SELECT ec.*
        FROM email_content ec
        WHERE ec.editor_id = :userId
          AND ec.is_active = true
          AND (
              :searchTerm IS NULL
              OR to_tsvector(
                    'portuguese',
                    COALESCE(ec.topic, '') || ' ' || COALESCE(ec.subject, '')
                 )
                 @@ plainto_tsquery('portuguese', :searchTerm)
          )
        """,
            countQuery = """
        SELECT COUNT(ec.id)
        FROM email_content ec
        WHERE ec.editor_id = :userId
          AND ec.is_active = true
          AND (
              :searchTerm IS NULL
              OR to_tsvector(
                    'portuguese',
                    COALESCE(ec.topic, '') || ' ' || COALESCE(ec.subject, '')
                 )
                 @@ plainto_tsquery('portuguese', :searchTerm)
          )
        """,
            nativeQuery = true
    )
    Page<EmailContent> findByEmailContentEditorAndIsActiveTrue(
            @Param("userId") UUID userId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("""
    UPDATE EmailContent ec
    SET ec.isActive = false,
        ec.disabledAt = CURRENT_TIMESTAMP
    WHERE ec = :emailContent
""")
    void setEmailContentAsNonActive(@Param("emailContent") EmailContent emailContent);

    @Query("""
    SELECT ec
    FROM EmailContent ec
    WHERE ec.editor.id = :userId
      AND ec.isActive = true
""")
    List<EmailContent> findAllByUserIdAndIsActiveTrue(
            @Param("userId") UUID userId
    );


}
