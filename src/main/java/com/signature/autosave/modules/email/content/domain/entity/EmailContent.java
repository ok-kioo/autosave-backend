package com.signature.autosave.modules.email.content.domain.entity;

import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "email_content")
public class EmailContent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    private String topic;

    @NotNull
    private String subject;

    @NotNull
    private String body;

    @ManyToOne
    @JoinColumn(name = "editor_id", referencedColumnName = "id")
    private User editor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull
    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "disabled_at")
    private LocalDateTime disabledAt;
}
