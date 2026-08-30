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
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignDeletedEvent;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        EmailCampaign emailCampaign = emailCampaignRepository.findByIdAndIsActiveTrue(createEmailCampaignReviewDTO.emailCampaign())
                .orElseThrow(() -> new IllegalArgumentException("Email campaign not found."));

        List<EmailCampaignReview> emailCampaignReviews = emailCampaignReviewRepository.findByEmailCampaignAndIsActiveTrue(emailCampaign);

        if(emailCampaignReviews.size() >= 2){
            throw new IllegalArgumentException("The pairs of evaluators for this campaign have already been defined.");
        }

        EmailCampaignReview emailCampaignReview = EmailCampaignReviewBuilder.builder()
                .withEmailCampaign(emailCampaign)
                .withComment(createEmailCampaignReviewDTO.comment())
                .withStatus(createEmailCampaignReviewDTO.status())
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
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        EmailCampaignReview emailCampaignReview = emailCampaignReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));

        if (emailCampaignReview.getEmailCampaign().isAvailable()){
            throw new IllegalArgumentException("You can not delete this review, this email campaign is already available.");
        }

        if (emailCampaignReview.getReviewer() != user) {
            throw new IllegalArgumentException("You do not have permission to update this review.");
        }

        Optional.ofNullable(updateEmailCampaignReviewDTO.comment()).ifPresent(emailCampaignReview::setComment);
        Optional.ofNullable(updateEmailCampaignReviewDTO.status()).ifPresent(emailCampaignReview::setStatus);

        emailCampaignReviewRepository.save(emailCampaignReview);

        List<EmailCampaignReview> emailCampaignReviews = emailCampaignReviewRepository.
                findByEmailCampaignAndIsActiveTrue(emailCampaignReview.getEmailCampaign());

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
                .orElseThrow(() -> new IllegalArgumentException("Email content not found."));

        return new EmailCampaignReviewResponseDTO(
                emailCampaignReview.getId(),
                emailCampaignReview.getStatus(),
                emailCampaignReview.getComment(),
                emailCampaignReview.getEmailCampaign()
        );
    }

    @Transactional(readOnly = true)
    public List<EmailCampaignReviewResponseDTO> listEmailCampaignReviews(UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return emailCampaignReviewRepository.findByReviewerAndIsActiveTrue(user).stream().map
                (emailCampaignReview -> new EmailCampaignReviewResponseDTO(
                emailCampaignReview.getId(),
                emailCampaignReview.getStatus(),
                emailCampaignReview.getComment(),
                emailCampaignReview.getEmailCampaign()
        )).toList();
    }

    @Transactional(readOnly = true)
    public List<EmailCampaignReviewResponseDTO> listEmailCampaignReviewsByEmailCampaign(UUID emailCampaignId, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        EmailCampaign emailCampaign = emailCampaignRepository.findByIdAndIsActiveTrue(emailCampaignId)
                .orElseThrow(() -> new IllegalArgumentException("Email campaign not found."));

        if(emailCampaign.getEmailContent().getEditor() != user){
            throw new IllegalArgumentException("You do not have permission to read this reviews.");

        }

        return emailCampaignReviewRepository.findByEmailCampaignAndIsActiveTrue(emailCampaign).stream().map(
                emailCampaignReview -> new EmailCampaignReviewResponseDTO(
                emailCampaignReview.getId(),
                emailCampaignReview.getStatus(),
                emailCampaignReview.getComment(),
                emailCampaignReview.getEmailCampaign()
        )).toList();
    }

    public void deleteEmailCampaignReview(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        EmailCampaignReview emailCampaignReview = emailCampaignReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));

        if (emailCampaignReview.getEmailCampaign().isAvailable()){
            throw new IllegalArgumentException("You can not delete this review, this email campaign is already available.");
        }

        if(emailCampaignReview.getReviewer() != user){
            throw new IllegalArgumentException("You do not have permission to delete this review.");
        }

        emailCampaignReviewRepository.setEmailCampaignReviewAsNonActive(emailCampaignReview);
    }

    private void emailCampaignApproved(EmailCampaign emailCampaign){
        publisher.publishEvent(new EmailCampaignApprovedEvent(emailCampaign.getId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void cascadeDeleteEmailCampaigns(EmailCampaignDeletedEvent event) {

        EmailCampaign emailCampaign = emailCampaignRepository.findById(event.emailCampaignId())
                .orElseThrow(() -> new RuntimeException("User not found."));

        emailCampaignReviewRepository
                .findAllByUserIdAndIsActiveTrue(emailCampaign.getEmailContent().getEditor().getId())
                .forEach(emailCampaignReviewRepository::setEmailCampaignReviewAsNonActive);
    }

}
