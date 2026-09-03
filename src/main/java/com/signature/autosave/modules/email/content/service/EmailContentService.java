package com.signature.autosave.modules.email.content.service;

import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaignReview;
import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;
import com.signature.autosave.modules.email.campaign.domain.repository.EmailCampaignReviewRepository;
import com.signature.autosave.modules.email.content.builder.EmailContentBuilder;
import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.email.content.domain.repository.EmailContentRepository;
import com.signature.autosave.modules.email.content.dto.CreateEmailContentDTO;
import com.signature.autosave.modules.email.content.dto.EmailContentResponseDTO;
import com.signature.autosave.modules.email.content.dto.UpdateEmailContentDTO;
import com.signature.autosave.modules.email.content.service.events.EmailContentDeletedEvent;
import com.signature.autosave.modules.email.content.service.events.EmailContentUpdatedEvent;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import com.signature.autosave.modules.user.service.events.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailContentService {
    private final EmailContentRepository emailContentRepository;
    private final EmailCampaignReviewRepository emailCampaignReviewRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    public EmailContentResponseDTO createEmailContent(CreateEmailContentDTO createEmailContentDTO, UserDetails userDetails){
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        EmailContent emailContent = EmailContentBuilder.builder()
                .withTopic(createEmailContentDTO.topic().name())
                .withSubject(createEmailContentDTO.title())
                .withBody(createEmailContentDTO.body())
                .withEditor(user)
                .withCreatedAt(LocalDateTime.now())
                .build();

        emailContentRepository.save(emailContent);

        return new EmailContentResponseDTO(
                emailContent.getId(),
                emailContent.getTopic(),
                emailContent.getSubject(),
                emailContent.getBody(),
                emailContent.getEditor(),
                emailContent.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public EmailContentResponseDTO listEmailContent(UUID id) {
        EmailContent emailContent = emailContentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Email content not found."));

        return new EmailContentResponseDTO(
                emailContent.getId(),
                emailContent.getTopic(),
                emailContent.getSubject(),
                emailContent.getBody(),
                emailContent.getEditor(),
                emailContent.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<EmailContentResponseDTO> listEmailContents(UserDetails userDetails, Pageable pageable, String searchTerm) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return emailContentRepository.findByEmailContentEditorAndIsActiveTrue(user.getId(), searchTerm, pageable).map(
                emailContent -> new EmailContentResponseDTO(
                emailContent.getId(),
                emailContent.getTopic(),
                emailContent.getSubject(),
                emailContent.getBody(),
                emailContent.getEditor(),
                emailContent.getCreatedAt()
        ));
    }

    public EmailContentResponseDTO updateEmailContent(UUID id, UpdateEmailContentDTO updateEmailContentDTO, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        EmailContent emailContent = emailContentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Email content not found."));

        if(emailContent.getEditor() != user){
            throw new IllegalArgumentException("You do not have permission to update this email content.");
        }

        List<EmailCampaignReview> emailCampaignReviews = emailCampaignReviewRepository.findByEmailContent(emailContent);

        if(!emailCampaignReviews.contains(EmailCampaignStatus.PENDING)){
            throw new IllegalArgumentException("The content may only be edited if requested by an reviewer.");
        }

        Optional.ofNullable(updateEmailContentDTO.topic())
                .ifPresent(topic -> emailContent.setTopic(topic.name()));
        Optional.ofNullable(updateEmailContentDTO.title())
                .ifPresent(emailContent::setSubject);
        Optional.ofNullable(updateEmailContentDTO.body())
                .ifPresent(emailContent::setBody);

        emailContentRepository.save(emailContent);

        emailCampaignReviews.forEach(emailCampaignReview -> {
            emailCampaignReview.setStatus(EmailCampaignStatus.UPDATED);
            emailCampaignReviewRepository.save(emailCampaignReview);
            publisher.publishEvent(new EmailContentUpdatedEvent(emailContent, emailCampaignReview.getId(),
                emailCampaignReview.getReviewer().getEmail()));
        });

        return new EmailContentResponseDTO(
                emailContent.getId(),
                emailContent.getTopic(),
                emailContent.getSubject(),
                emailContent.getBody(),
                emailContent.getEditor(),
                emailContent.getCreatedAt()
        );
    }

    public void deleteEmailContent(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        EmailContent emailContent = emailContentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Email content not found."));

        if(emailContent.getEditor() != user){
            throw new IllegalArgumentException("You do not have permission to delete this email content.");
        }

        emailContentRepository.setEmailContentAsNonActive(emailContent);
        publisher.publishEvent(new EmailContentDeletedEvent(emailContent.getId()));

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void cascadeDeleteEmailContents(UserDeletedEvent event) {

        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new RuntimeException("User not found."));

        emailContentRepository
                .findAllByUserIdAndIsActiveTrue(user.getId())
                .forEach(emailContent -> {
                    emailContentRepository.setEmailContentAsNonActive(emailContent);
                    publisher.publishEvent(new EmailContentDeletedEvent(emailContent.getId()));
                });
    }
}
