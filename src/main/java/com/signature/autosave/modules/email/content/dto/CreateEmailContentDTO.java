package com.signature.autosave.modules.email.content.dto;

import com.signature.autosave.modules.email.content.domain.enums.EmailTopic;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateEmailContentDTO {
    @NotNull
    private String title;
    @NotNull
    private EmailTopic topic;
    @NotNull
    private String body;
}
