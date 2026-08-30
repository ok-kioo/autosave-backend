package com.signature.autosave.modules.email.content.dto;

import com.signature.autosave.modules.email.content.domain.enums.EmailTopic;

public record UpdateEmailContentDTO (String title,
                                    EmailTopic topic,
                                    String body) {

}
