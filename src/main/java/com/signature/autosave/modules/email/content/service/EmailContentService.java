package com.signature.autosave.modules.email.content.service;

import com.signature.autosave.infra.components.email.IEmailComponent;
import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaignReview;
import com.signature.autosave.modules.email.campaign.domain.enums.EmailCampaignStatus;
import com.signature.autosave.modules.email.campaign.domain.repository.EmailCampaignReviewRepository;
import com.signature.autosave.modules.email.content.builder.EmailContentBuilder;
import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.email.content.domain.repository.EmailContentRepository;
import com.signature.autosave.modules.email.content.dto.CreateEmailContentDTO;
import com.signature.autosave.modules.email.content.dto.EmailContentResponseDTO;
import com.signature.autosave.modules.email.content.dto.UpdateEmailContentDTO;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final IEmailComponent emailComponent;

    @Value("${app.frontend.url}")
    private String frontEndUrl;

    public EmailContentResponseDTO createEmailContent(CreateEmailContentDTO createEmailContentDTO, UserDetails userDetails){
        User user = userRepository.findByEmail(userDetails.getUsername())
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
        EmailContent emailContent = emailContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Content not found."));

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
    public List<EmailContentResponseDTO> listEmailContents(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return emailContentRepository.findByEditor(user).stream().map(emailContent -> new EmailContentResponseDTO(
                emailContent.getId(),
                emailContent.getTopic(),
                emailContent.getSubject(),
                emailContent.getBody(),
                emailContent.getEditor(),
                emailContent.getCreatedAt()
        )).toList();
    }

    public EmailContentResponseDTO updateEmailContent(UUID id, UpdateEmailContentDTO updateEmailContentDTO, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        EmailContent emailContent = emailContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Content not found."));

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
            if(emailCampaignReview.getStatus() == EmailCampaignStatus.PENDING){
                emailCampaignReview.setStatus(EmailCampaignStatus.UPDATED);
                emailCampaignReviewRepository.save(emailCampaignReview);
                emailContentUpdatedNotify(emailContent, emailCampaignReview.getId(), emailCampaignReview.getReviewer().getEmail());
            }
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
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        EmailContent emailContent = emailContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Content not found."));

        if(emailContent.getEditor() != user){
            throw new IllegalArgumentException("You do not have permission to delete this email content.");
        }

        emailContentRepository.delete(emailContent);
    }

    private void emailContentUpdatedNotify(EmailContent emailContent, UUID emailCampaignReviewId, String emailCampaignReviewerEmail){

        String template = emailComponent.buildTemplate(
                emailContent.getEditor().getName(),
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                "Revisão de campanha de email",
                "Conteúdo de email atualizado",
                "O conteúdo do email '" + emailContent.getSubject() + "', referente a campanha revisada por você foi atualizado. " +
                        "Por favor, revise as alterações e aprove ou rejeite a campanha de email associada.",
                frontEndUrl+"/email/content/review"+emailCampaignReviewId
        );

        try {
            emailComponent.sendEmail(emailCampaignReviewerEmail,
                    "Conteúdo de email atualizado - Revisão de campanha de email",
                    template);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Erro ao enviar email de notificação", e);
        }
}
}
