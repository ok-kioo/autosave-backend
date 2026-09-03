package com.signature.autosave.infra.components.email;

import com.signature.autosave.infra.configuration.RabbitMQConfig;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationConsumer {
    private final IEmailComponent emailComponent;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consume(EmailNotificationMessage message) {

        log.info(
                "Processing email notification. publishId={}, type={}",
                message.publishId(),
                message.type()
        );

        try {
            switch (message.type()) {

                case TO -> emailComponent.sendEmailTo(
                        message.recipients().getFirst(),
                        message.subject(),
                        message.html()
                );

                case BCC -> emailComponent.sendEmailBcc(
                        message.recipients(),
                        message.subject(),
                        message.html()
                );
            }

        } catch (MessagingException e) {
            throw new RuntimeException(
                    "Failed to send email",
                    e
            );
        }
    }
}