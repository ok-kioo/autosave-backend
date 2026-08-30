package com.signature.autosave.modules.email.campaign.service;

import com.signature.autosave.infra.components.email.IEmailComponent;
import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import com.signature.autosave.modules.contract.domain.enums.BillingStatus;
import com.signature.autosave.modules.email.campaign.builder.EmailCampaignBuilder;
import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.email.campaign.domain.repository.EmailCampaignRepository;
import com.signature.autosave.modules.email.campaign.dto.CreateEmailCampaignDTO;
import com.signature.autosave.modules.email.campaign.dto.EmailCampaignResponseDTO;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignApprovedEvent;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignCreatedEvent;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignDeletedEvent;
import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.email.content.domain.repository.EmailContentRepository;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import com.signature.autosave.modules.subscription.domain.repository.SubscriptionPlanRepository;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailCampaignService {
    private final EmailCampaignRepository emailCampaignRepository;
    private final EmailContentRepository emailContentRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final IEmailComponent IEmailComponent;
    private final ApplicationEventPublisher publisher;

    @Value("${app.frontend.url}")
    private String frontEndUrl;

    @Transactional
    public EmailCampaignResponseDTO createEmailCampaign(CreateEmailCampaignDTO createEmailCampaignDTO, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        EmailContent emailContent = emailContentRepository.findByIdAndIsActiveTrue(createEmailCampaignDTO.emailContent())
                .orElseThrow(() -> new IllegalArgumentException("Email content not found."));

        if(emailContent.getEditor() != user){
            throw new IllegalArgumentException("The email content must be created by the user for use in a campaign.");
        }

        List<SubscriptionPlan> emailCampaignSubscriptionPlans = subscriptionPlanRepository.findEmailCampaignSubscriptionPlans(
                user.getPlanContract().getSubscriptionPlan().getPrice()
        );

        EmailCampaign emailCampaign = EmailCampaignBuilder.builder()
                .withTextPreview(createEmailCampaignDTO.textPreview())
                .withEmailContent(emailContent)
                .withSubscriptionPlans(emailCampaignSubscriptionPlans)
                .build();

        emailCampaignRepository.save(emailCampaign);

        publisher.publishEvent(new EmailCampaignCreatedEvent(emailCampaign.getId()));

        return new EmailCampaignResponseDTO(
                emailCampaign.getId(),
                emailCampaign.getTextPreview(),
                emailCampaign.getEmailContent(),
                emailCampaign.getSubscriptionPlans(),
                emailCampaign.isAvailable(),
                emailCampaign.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public EmailCampaignResponseDTO listEmailCampaign(UUID id) {
        EmailCampaign emailCampaign = emailCampaignRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Email content not found."));

        return new EmailCampaignResponseDTO(
                emailCampaign.getId(),
                emailCampaign.getTextPreview(),
                emailCampaign.getEmailContent(),
                emailCampaign.getSubscriptionPlans(),
                emailCampaign.isAvailable(),
                emailCampaign.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<EmailCampaignResponseDTO> listEmailCampaigns(UserDetails userDetails, Pageable pageable, String searchTerm) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return emailCampaignRepository.findByEmailContentEditorAndIsActiveTrue(user.getId(), searchTerm, pageable).map(emailCampaign -> new EmailCampaignResponseDTO(
                emailCampaign.getId(),
                emailCampaign.getTextPreview(),
                emailCampaign.getEmailContent(),
                emailCampaign.getSubscriptionPlans(),
                emailCampaign.isAvailable(),
                emailCampaign.getCreatedAt()
        ));
    }

    @Transactional(readOnly = true)
    public Page<EmailCampaignResponseDTO> listEmailCampaignsAvailable(UserDetails userDetails, Pageable pageable, String searchTerm) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        PlanContract userPlanContract = user.getPlanContract();

        if (userPlanContract.getStatus() != BillingStatus.PAID){
            throw new IllegalArgumentException("You do not have permission to read campaigns within a paid plan contract.");
        }

        UUID planId = userPlanContract
                .getSubscriptionPlan()
                .getId();

        return emailCampaignRepository.findAccessibleCampaigns(planId, searchTerm, pageable).map(
                emailCampaign -> new EmailCampaignResponseDTO(
                emailCampaign.getId(),
                emailCampaign.getTextPreview(),
                emailCampaign.getEmailContent(),
                emailCampaign.getSubscriptionPlans(),
                emailCampaign.isAvailable(),
                emailCampaign.getCreatedAt()
        ));
    }

    @Transactional
    public void deleteEmailCampaign(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        EmailCampaign emailCampaign = emailCampaignRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Email content not found."));

        if(emailCampaign.getEmailContent().getEditor() != user){
            throw new IllegalArgumentException("You do not have permission to delete this campaign.");
        }

        emailCampaignRepository.setEmailCampaignAsNonActive(emailCampaign);

        publisher.publishEvent(new EmailCampaignDeletedEvent(emailCampaign.getId()));
    }

    @EventListener
    public void handleEmailCampaignEvent(EmailCampaignApprovedEvent emailCampaignApprovedEvent) {
        EmailCampaign emailCampaign = emailCampaignRepository.findById(emailCampaignApprovedEvent.emailCampaign())
                .orElseThrow(() -> new IllegalArgumentException("Email content not found."));

        emailCampaign.setAvailable(true);
        emailCampaignRepository.save(emailCampaign);

        this.sendCampaign(emailCampaign);
    }

    private void sendCampaign(EmailCampaign emailCampaign){
        String template = IEmailComponent.buildTemplate(
                emailCampaign.getEmailContent().getEditor().getName(),
                emailCampaign.getEmailContent().getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                emailCampaign.getEmailContent().getTopic(),
                emailCampaign.getEmailContent().getSubject(),
                emailCampaign.getTextPreview(),
                frontEndUrl+"/email/content/"+emailCampaign.getId()
        );

        List<User> usersToSend = userRepository.findUsersEligibleForCampaign(emailCampaign.getId());
        usersToSend.forEach(user -> {
            try {
                IEmailComponent.sendEmail(
                        user.getEmail(),
                        emailCampaign.getEmailContent().getSubject(),
                        template
                );
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
