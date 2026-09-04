package com.signature.autosave.modules.email.content.dto;

import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmailContentResponseDTO (@NotNull
                                       UUID id,

                                       @NotNull
                                       String topic,

                                       @NotNull
                                       String subject,

                                       @NotNull
                                       String body,

                                       @NotNull
                                       User editor,

                                       @NotNull
                                       LocalDateTime createdAt) {

}
