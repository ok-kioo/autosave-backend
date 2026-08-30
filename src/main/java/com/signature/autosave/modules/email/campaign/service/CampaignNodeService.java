package com.signature.autosave.modules.email.campaign.service;

import com.signature.autosave.modules.email.campaign.domain.entity.node.CampaignNode;
import com.signature.autosave.modules.email.campaign.domain.repository.EmailCampaignRepository;
import com.signature.autosave.modules.email.campaign.domain.repository.node.CampaignNodeRepository;
import com.signature.autosave.modules.email.campaign.domain.repository.node.CommentNodeRepository;
import com.signature.autosave.modules.email.campaign.dto.node.*;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignCreatedEvent;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignDeletedEvent;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserNodeRepository;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignNodeService {
    private final EmailCampaignRepository emailCampaignRepository;
    private final CampaignNodeRepository campaignNodeRepository;
    private final UserNodeRepository userNodeRepository;
    private final CommentNodeRepository commentNodeRepository;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void registerCampaign(EmailCampaignCreatedEvent event) {
        emailCampaignRepository.findByIdAndIsActiveTrue(event.emailCampaign())
                .orElseThrow(() -> new RuntimeException("Email campaign not found."));

        CampaignNode campaignNode = new CampaignNode(event.emailCampaign());

        campaignNodeRepository.save(campaignNode);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteCampaign(EmailCampaignDeletedEvent event) {
        campaignNodeRepository.findById(event.emailCampaign())
                .orElseThrow(() -> new RuntimeException("Email campaign not found."));

        campaignNodeRepository.deleteById(event.emailCampaign());
    }

    public void registerView(RegisterCampaignNodeViewDTO registerCampaignNodeViewDTO, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found."));

        if(user.getId() != registerCampaignNodeViewDTO.userId()) {
            throw new RuntimeException("User Id does not match the authenticated user.");
        }

        campaignNodeRepository.findById(registerCampaignNodeViewDTO.emailCampaignId())
                .orElseThrow(() -> new RuntimeException("Email campaign not found."));

        userNodeRepository.findById(registerCampaignNodeViewDTO.userId())
                .orElseThrow(() -> new RuntimeException("User not found."));

        campaignNodeRepository.registerView(registerCampaignNodeViewDTO.userId(), registerCampaignNodeViewDTO.emailCampaignId());
    }

    public Long countViews(UUID emailCampaignId) {
        campaignNodeRepository.findById(emailCampaignId)
                .orElseThrow(() -> new RuntimeException("Email campaign not found."));

        return campaignNodeRepository.countViews(emailCampaignId);
    }

    public Boolean toggleLike(ToggleLikeCampaignNodeDTO toggleLikeCampaignNodeDTO, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found."));

        if(user.getId() != toggleLikeCampaignNodeDTO.userId()) {
            throw new RuntimeException("User Id does not match the authenticated user.");
        }

        campaignNodeRepository.findById(toggleLikeCampaignNodeDTO.emailCampaignId())
                .orElseThrow(() -> new RuntimeException("Email campaign not found."));

        userNodeRepository.findById(toggleLikeCampaignNodeDTO.userId())
                .orElseThrow(() -> new RuntimeException("User not found."));

        return campaignNodeRepository.toggleLike(toggleLikeCampaignNodeDTO.userId(), toggleLikeCampaignNodeDTO.emailCampaignId());
    }

    public Long countLikes(UUID campaignId) {
        campaignNodeRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Email campaign not found."));

        return campaignNodeRepository.countLikes(campaignId);
    }

    public CommentThreadProjection registerCampaignComment(RegisterCampaignCommentDTO registerCampaignCommentDTO, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found."));

        if(user.getId() != registerCampaignCommentDTO.userId()) {
            throw new RuntimeException("User Id does not match the authenticated user.");
        }

        campaignNodeRepository.findById(registerCampaignCommentDTO.emailCampaignId())
                .orElseThrow(() -> new RuntimeException("Email campaign not found."));

        userNodeRepository.findById(registerCampaignCommentDTO.userId())
                .orElseThrow(() -> new RuntimeException("User not found."));

        UUID newCommentId = UUID.randomUUID();

        return commentNodeRepository.registerCampaignComment(registerCampaignCommentDTO.userId(),
                registerCampaignCommentDTO.emailCampaignId(), newCommentId, registerCampaignCommentDTO.text());
    }

    public CommentThreadProjection registerReplyComment(RegisterReplyCommentDTO registerReplyCommentDTO, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found."));

        if(user.getId() != registerReplyCommentDTO.userId()) {
            throw new RuntimeException("User Id does not match the authenticated user.");
        }

        commentNodeRepository.findById(registerReplyCommentDTO.parentCommentId())
                .orElseThrow(() -> new RuntimeException("Father comment not found"));

        userNodeRepository.findById(registerReplyCommentDTO.userId())
                .orElseThrow(() -> new RuntimeException("User not found."));

        UUID newCommentId = UUID.randomUUID();

        return commentNodeRepository.replyComment(registerReplyCommentDTO.userId(),
                registerReplyCommentDTO.parentCommentId(), newCommentId, registerReplyCommentDTO.text());
    }

    public List<CommentThreadProjection> listComments(UUID campaignId) {
        campaignNodeRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Email campaign not found."));

        return commentNodeRepository.findComments(campaignId);
    }

    public void deleteComment(UUID commentId, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found."));


        commentNodeRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if(commentNodeRepository.commentWrittenByUser(commentId, user.getId())) {
            throw new RuntimeException("You do not have permission to delete this comment.");
        }

        commentNodeRepository.deleteComment(commentId);
    }


}
