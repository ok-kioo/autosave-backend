package com.signature.autosave.modules.email.campaign.dto.node;

import com.signature.autosave.modules.email.campaign.domain.entity.node.CommentNode;
import com.signature.autosave.modules.user.domain.entity.node.UserNode;

import java.util.List;

public interface CommentThreadProjection {

    CommentNode getRoot();

    UserNode getAuthorRoot();

    List<ReplyProjection> getReplies();

    interface ReplyProjection {
        CommentNode getComment();
        UserNode getAuthor();
    }
}
