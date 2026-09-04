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
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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

    @EventListener
    public void registerCampaign(EmailCampaignCreatedEvent event) {

        emailCampaignRepository.findByIdAndIsActiveTrue(event.emailCampaign())
                .orElseThrow(() -> new RuntimeException("Email campaign not found."));

        CampaignNode campaignNode = new CampaignNode(
                event.emailCampaign(),
                null,
                true
        );

        campaignNodeRepository.save(campaignNode);
    }

    @EventListener
    public void deleteCampaign(EmailCampaignDeletedEvent event) {

        campaignNodeRepository.findById(event.emailCampaignId()).orElseThrow(() -> new RuntimeException("Campaign node not found."));

        commentNodeRepository.softDeleteCampaignComments(
                event.emailCampaignId()
        );

        campaignNodeRepository.softDelete(
                event.emailCampaignId()
        );
    }

    public void registerView(
            RegisterCampaignNodeViewDTO dto,
            UserDetails userDetails
    ) {

        User user = userRepository
                .findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("User not found.")
                );

        if (!user.getId().equals(dto.userId())) {
            throw new RuntimeException(
                    "User Id does not match the authenticated user."
            );
        }

        campaignNodeRepository
                .findActiveById(dto.emailCampaignId())
                .orElseThrow(() ->
                        new RuntimeException("Email campaign not found.")
                );

        userNodeRepository
                .findActiveById(dto.userId())
                .orElseThrow(() ->
                        new RuntimeException("User not found.")
                );

        campaignNodeRepository.registerView(
                dto.userId(),
                dto.emailCampaignId()
        );
    }

    public Long countViews(UUID campaignId) {

        campaignNodeRepository
                .findActiveById(campaignId)
                .orElseThrow(() ->
                        new RuntimeException("Email campaign not found.")
                );

        return campaignNodeRepository.countViews(campaignId);
    }

    public Boolean toggleLike(
            ToggleLikeCampaignNodeDTO dto,
            UserDetails userDetails
    ) {

        User user = userRepository
                .findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("User not found.")
                );

        if (!user.getId().equals(dto.userId())) {
            throw new RuntimeException(
                    "User Id does not match the authenticated user."
            );
        }

        campaignNodeRepository
                .findActiveById(dto.emailCampaignId())
                .orElseThrow(() ->
                        new RuntimeException("Email campaign not found.")
                );

        userNodeRepository.findActiveById(dto.userId())
                .orElseThrow(() ->
                        new RuntimeException("User not found.")
                );

        return campaignNodeRepository.toggleLike(
                dto.userId(),
                dto.emailCampaignId()
        );
    }

    public Long countLikes(UUID campaignId) {

        campaignNodeRepository
                .findActiveById(campaignId)
                .orElseThrow(() ->
                        new RuntimeException("Email campaign not found.")
                );

        return campaignNodeRepository.countLikes(campaignId);
    }

    public CommentThreadProjection registerCampaignComment(
            RegisterCampaignCommentDTO dto,
            UserDetails userDetails
    ) {

        User user = userRepository
                .findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("User not found.")
                );

        if (!user.getId().equals(dto.userId())) {
            throw new RuntimeException(
                    "User Id does not match the authenticated user."
            );
        }

        campaignNodeRepository
                .findActiveById(dto.emailCampaignId())
                .orElseThrow(() ->
                        new RuntimeException("Email campaign not found.")
                );

        userNodeRepository
                .findActiveById(dto.userId())
                .orElseThrow(() ->
                        new RuntimeException("User not found.")
                );

        UUID commentId = UUID.randomUUID();

        return commentNodeRepository.registerCampaignComment(
                dto.userId(),
                dto.emailCampaignId(),
                commentId,
                dto.text()
        );
    }

    public CommentThreadProjection registerReplyComment(
            RegisterReplyCommentDTO dto,
            UserDetails userDetails
    ) {

        User user = userRepository
                .findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("User not found.")
                );

        if (!user.getId().equals(dto.userId())) {
            throw new RuntimeException(
                    "User Id does not match the authenticated user."
            );
        }

        commentNodeRepository
                .findActiveById(dto.parentCommentId())
                .orElseThrow(() ->
                        new RuntimeException("Parent comment not found.")
                );

        userNodeRepository
                .findActiveById(dto.userId())
                .orElseThrow(() ->
                        new RuntimeException("User not found.")
                );

        UUID commentId = UUID.randomUUID();

        return commentNodeRepository.replyComment(
                dto.userId(),
                dto.parentCommentId(),
                commentId,
                dto.text()
        );
    }

    public List<CommentThreadProjection> listComments(
            UUID campaignId,
            Pageable pageable
    ) {

        campaignNodeRepository
                .findActiveById(campaignId)
                .orElseThrow(() ->
                        new RuntimeException("Email campaign not found.")
                );

        long skip = pageable.getOffset();
        int limit = pageable.getPageSize();

        return commentNodeRepository.findComments(
                campaignId,
                skip,
                limit
        );
    }

    public void deleteComment(
            UUID commentId,
            UserDetails userDetails
    ) {

        User user = userRepository
                .findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("User not found.")
                );

        commentNodeRepository
                .findActiveById(commentId)
                .orElseThrow(() ->
                        new RuntimeException("Comment not found.")
                );

        Boolean writtenByUser =
                commentNodeRepository.commentWrittenByUser(
                        commentId,
                        user.getId()
                );

        if (!Boolean.TRUE.equals(writtenByUser)) {
            throw new RuntimeException(
                    "You do not have permission to delete this comment."
            );
        }

        commentNodeRepository.softDelete(commentId);
    }
}
