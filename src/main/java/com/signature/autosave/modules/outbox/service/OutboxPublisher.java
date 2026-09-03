package com.signature.autosave.modules.outbox.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignSendEvent;
import com.signature.autosave.modules.email.content.service.events.EmailContentUpdatedEvent;
import com.signature.autosave.modules.outbox.domain.entity.OutboxEvent;
import com.signature.autosave.modules.outbox.domain.enums.OutboxStatus;
import com.signature.autosave.modules.outbox.domain.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private ApplicationEventPublisher publisher;

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {

        List<OutboxEvent> events = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : events) {

            try {

                switch (event.getEventType()) {
                    case "EmailContentUpdatedEvent":
                        EmailContentUpdatedEvent emailContentEvent = objectMapper.readValue(
                                event.getPayload(), EmailContentUpdatedEvent.class
                        );


                        publisher.publishEvent(new EmailContentUpdatedEvent(event.getId(), emailContentEvent.emailContent(),
                                emailContentEvent.emailCampaignReviewId(), emailContentEvent.emailCampaignReviewerEmail()));
                        break;

                    case "EmailCampaignSendEvent":
                        EmailCampaignSendEvent emailCampaignEvent = objectMapper.readValue(
                                event.getPayload(), EmailCampaignSendEvent.class
                        );
                        publisher.publishEvent(new EmailCampaignSendEvent(event.getId(), emailCampaignEvent.emailCampaignId(),
                                emailCampaignEvent.emailContent(), emailCampaignEvent.textPreview(), emailCampaignEvent.usersToSend()));
                        break;

                    default:
                        log.info("unidentified event");
                }

                event.setStatus(OutboxStatus.PUBLISHED);
                outboxRepository.save(event);

            } catch (Exception e) {
                log.info("Processing event={} again", event.getId());
            }
        }
    }

}
