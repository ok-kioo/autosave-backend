package com.signature.autosave.modules.email.content.dto;

import com.signature.autosave.modules.email.content.domain.enums.EmailTopic;
import jakarta.validation.constraints.NotNull;


public record CreateEmailContentDTO (@NotNull
                                     String title,
                                     @NotNull
                                     EmailTopic topic,
                                     @NotNull
                                     String body) {

}
