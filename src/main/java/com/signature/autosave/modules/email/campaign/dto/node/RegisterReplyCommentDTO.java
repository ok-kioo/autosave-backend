package com.signature.autosave.modules.email.campaign.dto.node;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record RegisterReplyCommentDTO(@NotNull
                                      UUID userId,

                                      @NotNull
                                      UUID parentCommentId,

                                      @NotNull
                                      String text) {


}
