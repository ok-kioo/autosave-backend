package com.signature.autosave.modules.email.campaign.service;

import com.signature.autosave.modules.email.campaign.builder.EmailCampaignReviewBuilder;
import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaignReview;
import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;
import com.signature.autosave.modules.email.campaign.domain.repository.EmailCampaignRepository;
import com.signature.autosave.modules.email.campaign.domain.repository.EmailCampaignReviewRepository;
import com.signature.autosave.modules.email.campaign.dto.CreateEmailCampaignReviewDTO;
import com.signature.autosave.modules.email.campaign.dto.EmailCampaignReviewResponseDTO;
import com.signature.autosave.modules.email.campaign.dto.UpdateEmailCampaignReviewDTO;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignApprovedEvent;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailCampaignReviewService {
    private final EmailCampaignReviewRepository emailCampaignReviewRepository;
    private final EmailCampaignRepository emailCampaignRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;


    public EmailCampaignReviewResponseDTO createEmailCampaignReview(CreateEmailCampaignReviewDTO createEmailCampaignReviewDTO, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        EmailCampaign emailCampaign = emailCampaignRepository.findById(createEmailCampaignReviewDTO.getEmailCampaign())
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo de email não encontrado"));

        List<EmailCampaignReview> emailCampaignReviews = emailCampaignReviewRepository.findByEmailCampaign(emailCampaign);

        if(emailCampaignReviews.size() >= 2){
            throw new IllegalArgumentException("Essa campanha já tem os pares de avaliadores definidos");
        }

        EmailCampaignReview emailCampaignReview = EmailCampaignReviewBuilder.builder()
                .withEmailCampaign(emailCampaign)
                .withComment(createEmailCampaignReviewDTO.getComment())
                .withStatus(createEmailCampaignReviewDTO.getStatus())
                .withReviewer(user)
                .build();

        emailCampaignReviewRepository.save(emailCampaignReview);

        if(emailCampaignReviews.contains(EmailCampaignStatus.APPROVED) && emailCampaignReview.getStatus() == EmailCampaignStatus.APPROVED){
            emailCampaignApproved(emailCampaign);
        }

        return new EmailCampaignReviewResponseDTO(
                emailCampaignReview.getId(),
                emailCampaignReview.getStatus(),
                emailCampaignReview.getComment(),
                emailCampaignReview.getEmailCampaign()
        );
    }

    public EmailCampaignReviewResponseDTO updateEmailCampaignReview(UUID id, UpdateEmailCampaignReviewDTO updateEmailCampaignReviewDTO, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        EmailCampaignReview emailCampaignReview = emailCampaignReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliação de campanha de email não encontrada"));

        if (emailCampaignReview.getReviewer() != user) {
            throw new IllegalArgumentException("Apenas o revisor pode atualizar a avaliação");
        }

        Optional.ofNullable(updateEmailCampaignReviewDTO.getComment()).ifPresent(emailCampaignReview::setComment);
        Optional.ofNullable(updateEmailCampaignReviewDTO.getStatus()).ifPresent(emailCampaignReview::setStatus);

        emailCampaignReviewRepository.save(emailCampaignReview);

        List<EmailCampaignReview> emailCampaignReviews = emailCampaignReviewRepository.findByEmailCampaign(emailCampaignReview.getEmailCampaign());

        if (emailCampaignReviews.contains(EmailCampaignStatus.APPROVED) && emailCampaignReview.getStatus() == EmailCampaignStatus.APPROVED) {
            emailCampaignApproved(emailCampaignReview.getEmailCampaign());
        }

        return new EmailCampaignReviewResponseDTO(
                emailCampaignReview.getId(),
                emailCampaignReview.getStatus(),
                emailCampaignReview.getComment(),
                emailCampaignReview.getEmailCampaign()
        );
    }

    @Transactional(readOnly = true)
    public EmailCampaignReviewResponseDTO listEmailCampaignReview(UUID id) {
        EmailCampaignReview emailCampaignReview = emailCampaignReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo não encontrado"));

        return new EmailCampaignReviewResponseDTO(
                emailCampaignReview.getId(),
                emailCampaignReview.getStatus(),
                emailCampaignReview.getComment(),
                emailCampaignReview.getEmailCampaign()
        );
    }

    @Transactional(readOnly = true)
    public List<EmailCampaignReviewResponseDTO> listEmailCampaignReviews(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return emailCampaignReviewRepository.findByReviewer(user).stream().map(emailCampaignReview -> new EmailCampaignReviewResponseDTO(
                emailCampaignReview.getId(),
                emailCampaignReview.getStatus(),
                emailCampaignReview.getComment(),
                emailCampaignReview.getEmailCampaign()
        )).toList();
    }

    @Transactional(readOnly = true)
    public List<EmailCampaignReviewResponseDTO> listEmailCampaignReviewsByEmailCampaign(UUID emailCampaignId, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        EmailCampaign emailCampaign = emailCampaignRepository.findById(emailCampaignId)
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo de email não encontrado"));

        if(emailCampaign.getEmailContent().getEditor() != user){
            throw new IllegalArgumentException("Apenas o editor do conteúdo de email pode acessar as avaliações da campanha");

        }

        return emailCampaignReviewRepository.findByEmailCampaign(emailCampaign).stream().map(emailCampaignReview -> new EmailCampaignReviewResponseDTO(
                emailCampaignReview.getId(),
                emailCampaignReview.getStatus(),
                emailCampaignReview.getComment(),
                emailCampaignReview.getEmailCampaign()
        )).toList();
    }

    public void deleteEmailCampaignReview(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        EmailCampaignReview emailCampaignReview = emailCampaignReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Revisão não encontrada"));

        if(emailCampaignReview.getReviewer() != user){
            throw new IllegalArgumentException("Apenas o editor da revisão pode deletá-la");
        }

        emailCampaignReviewRepository.delete(emailCampaignReview);
    }

    private void emailCampaignApproved(EmailCampaign emailCampaign){
        publisher.publishEvent(new EmailCampaignApprovedEvent(emailCampaign.getId()));
    }

}
