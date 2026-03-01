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
        emailCampaignRepository.findById(event.emailCampaign())
                .orElseThrow(() -> new RuntimeException("Campanha não encontrada"));

        CampaignNode campaignNode = new CampaignNode(event.emailCampaign());

        campaignNodeRepository.save(campaignNode);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteCampaign(EmailCampaignDeletedEvent event) {
        campaignNodeRepository.findById(event.emailCampaign())
                .orElseThrow(() -> new RuntimeException("Campanha não encontrada"));

        campaignNodeRepository.deleteById(event.emailCampaign());
    }

    public void registerView(RegisterCampaignNodeViewDTO registerCampaignNodeViewDTO, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if(user.getId() != registerCampaignNodeViewDTO.getUserId()) {
            throw new RuntimeException("Usuário autenticado não corresponde ao usuário fornecido");
        }

        campaignNodeRepository.findById(registerCampaignNodeViewDTO.getEmailCampaignId())
                .orElseThrow(() -> new RuntimeException("Campanha não encontrada"));

        userNodeRepository.findById(registerCampaignNodeViewDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        campaignNodeRepository.registerView(registerCampaignNodeViewDTO.getUserId(), registerCampaignNodeViewDTO.getEmailCampaignId());
    }

    public Long countViews(UUID emailCampaignId) {
        campaignNodeRepository.findById(emailCampaignId)
                .orElseThrow(() -> new RuntimeException("Campanha não encontrada"));

        return campaignNodeRepository.countViews(emailCampaignId);
    }

    public Boolean toggleLike(ToggleLikeCampaignNodeDTO toggleLikeCampaignNodeDTO, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if(user.getId() != toggleLikeCampaignNodeDTO.getUserId()) {
            throw new RuntimeException("Usuário autenticado não corresponde ao usuário fornecido");
        }

        campaignNodeRepository.findById(toggleLikeCampaignNodeDTO.getEmailCampaignId())
                .orElseThrow(() -> new RuntimeException("Campanha não encontrada"));

        userNodeRepository.findById(toggleLikeCampaignNodeDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return campaignNodeRepository.toggleLike(toggleLikeCampaignNodeDTO.getUserId(), toggleLikeCampaignNodeDTO.getEmailCampaignId());
    }

    public Long countLikes(UUID campaignId) {
        campaignNodeRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campanha não encontrada"));

        return campaignNodeRepository.countLikes(campaignId);
    }

    public CommentThreadProjection registerCampaignComment(RegisterCampaignCommentDTO registerCampaignCommentDTO, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if(user.getId() != registerCampaignCommentDTO.getUserId()) {
            throw new RuntimeException("Usuário autenticado não corresponde ao usuário fornecido");
        }

        campaignNodeRepository.findById(registerCampaignCommentDTO.getEmailCampaignId())
                .orElseThrow(() -> new RuntimeException("Campanha não encontrada"));

        userNodeRepository.findById(registerCampaignCommentDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        UUID newCommentId = UUID.randomUUID();

        return commentNodeRepository.registerCampaignComment(registerCampaignCommentDTO.getUserId(), registerCampaignCommentDTO.getEmailCampaignId(), newCommentId, registerCampaignCommentDTO.getText());
    }

    public CommentThreadProjection registerReplyComment(RegisterReplyCommentDTO registerReplyCommentDTO, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if(user.getId() != registerReplyCommentDTO.getUserId()) {
            throw new RuntimeException("Usuário autenticado não corresponde ao usuário fornecido");
        }

        commentNodeRepository.findById(registerReplyCommentDTO.getParentCommentId())
                .orElseThrow(() -> new RuntimeException("Comentário pai não encontrado"));

        userNodeRepository.findById(registerReplyCommentDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        UUID newCommentId = UUID.randomUUID();

        return commentNodeRepository.replyComment(registerReplyCommentDTO.getUserId(), registerReplyCommentDTO.getParentCommentId(), newCommentId, registerReplyCommentDTO.getText());
    }

    public List<CommentThreadProjection> listComments(UUID campaignId) {
        campaignNodeRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campanha não encontrada"));

        return commentNodeRepository.findComments(campaignId);
    }

    public void deleteComment(UUID commentId, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));


        commentNodeRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));

        if(commentNodeRepository.commentWrittenByUser(commentId, user.getId())) {
            throw new RuntimeException("Usuário autenticado não é o autor do comentário");
        }

        commentNodeRepository.deleteComment(commentId);
    }


}
