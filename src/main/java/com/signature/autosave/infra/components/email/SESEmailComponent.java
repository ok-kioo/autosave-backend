package com.signature.autosave.infra.components.email;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SESEmailComponent implements IEmailComponent {

    private final SesClient sesClient;

    @Value("${autosave.mail}")
    private String from;

    @Override
    public void sendEmailTo(String to, String subject, String html) {
        Destination destination = Destination.builder()
                .toAddresses(to)
                .build();

        Content subjectContent = Content.builder()
                .data(subject)
                .charset("UTF-8")
                .build();

        Content htmlContent = Content.builder()
                .data(html)
                .charset("UTF-8")
                .build();

        Body body = Body.builder()
                .html(htmlContent)
                .build();

        Message message = Message.builder()
                .subject(subjectContent)
                .body(body)
                .build();

        SendEmailRequest request = SendEmailRequest.builder()
                .source(from)
                .destination(destination)
                .message(message)
                .build();

        SendEmailResponse response = sesClient.sendEmail(request);

        log.info(
                "Email sent through SES. messageId={}",
                response.messageId()
        );
    }

    @Override
    public void sendEmailBcc(List<String> bcc, String subject, String html) throws MessagingException {
        Destination destination = Destination.builder()
                .bccAddresses(bcc)
                .build();

        Content subjectContent = Content.builder()
                .data(subject)
                .charset("UTF-8")
                .build();

        Content htmlContent = Content.builder()
                .data(html)
                .charset("UTF-8")
                .build();

        Body body = Body.builder()
                .html(htmlContent)
                .build();

        Message message = Message.builder()
                .subject(subjectContent)
                .body(body)
                .build();

        SendEmailRequest request = SendEmailRequest.builder()
                .source(from)
                .destination(destination)
                .message(message)
                .build();

        SendEmailResponse response = sesClient.sendEmail(request);

        log.info(
                "Email sent through SES. messageId={}",
                response.messageId()
        );
    }

}
