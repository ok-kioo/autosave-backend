package com.signature.autosave.persistence.builder;

import com.signature.autosave.persistence.entity.EmailContent;
import com.signature.autosave.persistence.entity.EmailRecord;
import jakarta.validation.constraints.NotBlank;

public class EmailRecordBuilder {
    private EmailContent emailContent;

    public static EmailRecordBuilder builder() {
        return new EmailRecordBuilder();
    }

    public EmailRecordBuilder withEmailContent(@NotBlank EmailContent emailContent) {
        this.emailContent = emailContent;
        return this;
    }

    public EmailRecord build() {
        EmailRecord emailContent = new EmailRecord();
        emailContent.setViewCount(0);
        emailContent.setEmailContent(this.emailContent);
        return emailContent;
    }
}
