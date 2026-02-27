package com.signature.autosave.modules.email.content.dto;

import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class EmailContentResponseDTO {
    @NotNull
    private UUID id;

    @NotNull
    private String topic;

    @NotNull
    private String subject;

    @NotNull
    private String body;

    @NotNull
    private User editor;

    @NotNull
    private LocalDateTime createdAt;

    public EmailContentResponseDTO(UUID id, String topic, String subject, String body, User editor, LocalDateTime createdAt) {
        this.id = id;
        this.topic = topic;
        this.subject = subject;
        this.body = body;
        this.editor = editor;
        this.createdAt = createdAt;
    }
}
