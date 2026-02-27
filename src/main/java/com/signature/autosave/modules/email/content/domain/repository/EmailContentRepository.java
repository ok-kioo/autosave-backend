package com.signature.autosave.modules.email.content.domain.repository;

import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmailContentRepository extends JpaRepository<EmailContent, UUID> {
    List<EmailContent> findByEditor(User user);
}
