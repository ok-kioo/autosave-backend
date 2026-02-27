package com.signature.autosave.modules.email.content.service;

import com.signature.autosave.modules.email.content.builder.EmailContentBuilder;
import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.email.content.domain.repository.EmailContentRepository;
import com.signature.autosave.modules.email.content.dto.CreateEmailContentDTO;
import com.signature.autosave.modules.email.content.dto.EmailContentResponseDTO;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailContentService {
    private final EmailContentRepository emailContentRepository;
    private final UserRepository userRepository;
    /*private final IEmailComponent IEmailComponent;

    @Value("${app.frontend.url}")
    private String frontEndUrl;

    String template = IEmailComponent.buildTemplate(
            user.getName(),
            emailContent.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
            createEmailContentDTO.getTopic(),
            createEmailContentDTO.getTitle(),
            createEmailContentDTO.getText1(),
            createEmailContentDTO.getText2(),
            frontEndUrl+"/email/content/"+emailContent.getId()
    );*/

    public EmailContentResponseDTO createEmailContent(CreateEmailContentDTO createEmailContentDTO, UserDetails userDetails){
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        String body = buildEmailBody(createEmailContentDTO);

        EmailContent emailContent = EmailContentBuilder.builder()
                .withTopic(createEmailContentDTO.getTopic())
                .withSubject(createEmailContentDTO.getTitle())
                .withBody(body)
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

    public EmailContentResponseDTO listEmailContent(UUID id) {
        EmailContent emailContent = emailContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo não encontrado"));

        return new EmailContentResponseDTO(
                emailContent.getId(),
                emailContent.getTopic(),
                emailContent.getSubject(),
                emailContent.getBody(),
                emailContent.getEditor(),
                emailContent.getCreatedAt()
        );
    }

    public List<EmailContentResponseDTO> listEmailContents(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return emailContentRepository.findByEditor(user).stream().map(emailContent -> new EmailContentResponseDTO(
                emailContent.getId(),
                emailContent.getTopic(),
                emailContent.getSubject(),
                emailContent.getBody(),
                emailContent.getEditor(),
                emailContent.getCreatedAt()
        )).toList();
    }

    public void deleteEmailContent(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        EmailContent emailContent = emailContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo não encontrado"));

        if(emailContent.getEditor() != user){
            throw new IllegalArgumentException("Apenas o editor do conteúdo pode deletá-lo");
        }

        emailContentRepository.delete(emailContent);
    }

    private String buildEmailBody(CreateEmailContentDTO createEmailContentDTO) {
        return """
                %s

                %s

                %s

                %s
                """.formatted(
                createEmailContentDTO.getText1(),
                createEmailContentDTO.getText2(),
                createEmailContentDTO.getText3(),
                createEmailContentDTO.getText4()
        );
    }

}
