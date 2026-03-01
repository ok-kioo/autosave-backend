package com.signature.autosave.modules.email.content.dto;

import com.signature.autosave.modules.email.content.domain.enums.EmailTopic;
import lombok.Getter;

@Getter
public class UpdateEmailContentDTO {
    private String title;
    private EmailTopic topic;
    private String body;
}
