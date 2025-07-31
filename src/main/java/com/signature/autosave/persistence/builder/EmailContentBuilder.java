package com.signature.autosave.persistence.builder;

import com.signature.autosave.persistence.entity.EmailContent;
import jakarta.validation.constraints.NotBlank;

public class EmailContentBuilder {
    private String title;
    private String source;
    private String destination;
    private String content;
    private String body;


    public static EmailContentBuilder builder() {
        return new EmailContentBuilder();
    }

    public EmailContentBuilder withTitle(@NotBlank String title) {
        this.title = title;
        return this;
    }

    public EmailContentBuilder withSource(String source) {
        this.source = source;
        return this;
    }

    public EmailContentBuilder withDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public EmailContentBuilder withContent(@NotBlank String content) {
        this.content = content;
        return this;
    }

    public EmailContentBuilder withBody(@NotBlank String body) {
        this.body = body;
        return this;
    }

    public EmailContent build() {
        EmailContent emailContent = new EmailContent();
        emailContent.setTitle(this.title);
        emailContent.setSource(this.source);
        emailContent.setDestination(this.destination);
        emailContent.setContent(this.content);
        emailContent.setBody(this.body);
        return emailContent;
    }
}
