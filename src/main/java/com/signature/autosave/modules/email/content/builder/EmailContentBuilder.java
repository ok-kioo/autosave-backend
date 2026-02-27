package com.signature.autosave.modules.email.content.builder;

import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class EmailContentBuilder {
    private String topic;
    private String subject;
    private String body;
    private User editor;
    private LocalDateTime createdAt;

    private EmailContentBuilder() {
    }

    public static EmailContentBuilder builder() {
        return new EmailContentBuilder();
    }

    public EmailContentBuilder withTopic(@NotNull String topic) {
        this.topic = topic;
        return this;
    }

    public EmailContentBuilder withSubject(@NotNull String subject) {
        this.subject = subject;
        return this;
    }

    public EmailContentBuilder withBody(@NotNull String body) {
        this.body = body;
        return this;
    }

    public EmailContentBuilder withEditor(User editor) {
        this.editor = editor;
        return this;
    }

    public EmailContentBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public EmailContent build() {
        EmailContent emailContent = new EmailContent();
        emailContent.setTopic(this.topic);
        emailContent.setSubject(this.subject);
        emailContent.setBody(this.body);
        emailContent.setEditor(this.editor);
        emailContent.setCreatedAt(this.createdAt);
        return emailContent;
    }
}
