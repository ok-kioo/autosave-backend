package com.signature.autosave.modules.email.campaign.service;

import com.signature.autosave.infra.components.email.IEmailComponent;
import com.signature.autosave.modules.email.campaign.builder.EmailCampaignBuilder;
import com.signature.autosave.modules.email.campaign.domain.entity.EmailCampaign;
import com.signature.autosave.modules.email.campaign.domain.repository.EmailCampaignRepository;
import com.signature.autosave.modules.email.campaign.dto.CreateEmailCampaignDTO;
import com.signature.autosave.modules.email.campaign.dto.EmailCampaignResponseDTO;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignApprovedEvent;
import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.email.content.domain.repository.EmailContentRepository;
import com.signature.autosave.modules.subscription.domain.entity.SubscriptionPlan;
import com.signature.autosave.modules.subscription.domain.repository.SubscriptionPlanRepository;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
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

    @Value("${app.frontend.url}")
    private String frontEndUrl;

    public EmailCampaignResponseDTO createEmailCampaign(CreateEmailCampaignDTO createEmailCampaignDTO, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        EmailContent emailContent = emailContentRepository.findById(createEmailCampaignDTO.getEmailContent())
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo de email não encontrado"));

        if(emailContent.getEditor() != user){
            throw new IllegalArgumentException("O conteúdo de email deve ser criado pelo usuário para ser utilizado em uma campanha");
        }

        List<SubscriptionPlan> emailCampaignSubscriptionPlans = subscriptionPlanRepository.findEmailCampaignSubscriptionPlans(
                user.getPlanContract().getSubscriptionPlan().getPrice()
        );

        EmailCampaign emailCampaign = EmailCampaignBuilder.builder()
                .withTextPreview(createEmailCampaignDTO.getTextPreview())
                .withEmailContent(emailContent)
                .withSubscriptionPlans(emailCampaignSubscriptionPlans)
                .build();

        emailCampaignRepository.save(emailCampaign);

        return new EmailCampaignResponseDTO(
                emailCampaign.getId(),
                emailCampaign.getTextPreview(),
                emailCampaign.getEmailContent(),
                emailCampaign.getSubscriptionPlans(),
                emailCampaign.isActive()
        );
    }

    @Transactional(readOnly = true)
    public EmailCampaignResponseDTO listEmailCampaign(UUID id) {
        EmailCampaign emailCampaign = emailCampaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo não encontrado"));

        return new EmailCampaignResponseDTO(
                emailCampaign.getId(),
                emailCampaign.getTextPreview(),
                emailCampaign.getEmailContent(),
                emailCampaign.getSubscriptionPlans(),
                emailCampaign.isActive()
        );
    }

    @Transactional(readOnly = true)
    public List<EmailCampaignResponseDTO> listEmailCampaigns(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return emailCampaignRepository.findByEmailContentEditor(user).stream().map(emailCampaign -> new EmailCampaignResponseDTO(
                emailCampaign.getId(),
                emailCampaign.getTextPreview(),
                emailCampaign.getEmailContent(),
                emailCampaign.getSubscriptionPlans(),
                emailCampaign.isActive()
        )).toList();
    }

    @Transactional(readOnly = true)
    public List<EmailCampaignResponseDTO> listEmailCampaignsAvailable(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return emailCampaignRepository.findAccessibleCampaignsByUser(user).stream().map(emailCampaign -> new EmailCampaignResponseDTO(
                emailCampaign.getId(),
                emailCampaign.getTextPreview(),
                emailCampaign.getEmailContent(),
                emailCampaign.getSubscriptionPlans(),
                emailCampaign.isActive()
        )).toList();
    }

    public void deleteEmailCampaign(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        EmailCampaign emailCampaign = emailCampaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo não encontrado"));

        if(emailCampaign.getEmailContent().getEditor() != user){
            throw new IllegalArgumentException("Apenas o editor do conteúdo pode deletá-lo");
        }

        emailCampaignRepository.delete(emailCampaign);
    }

    @EventListener
    public void handleEmailCampaignEvent(EmailCampaignApprovedEvent emailCampaignApprovedEvent) {
        EmailCampaign emailCampaign = emailCampaignRepository.findById(emailCampaignApprovedEvent.emailCampaign())
                .orElseThrow(() -> new IllegalArgumentException("Campanha de email não encontrada"));

        emailCampaign.setActive(true);
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
