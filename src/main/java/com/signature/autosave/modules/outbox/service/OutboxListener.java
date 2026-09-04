package com.signature.autosave.modules.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signature.autosave.modules.outbox.domain.entity.OutboxEvent;
import com.signature.autosave.modules.outbox.domain.repository.OutboxRepository;
import com.signature.autosave.modules.outbox.service.events.OutboxEmailCampaignSendEvent;
import com.signature.autosave.modules.outbox.service.events.OutboxEmailContentUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxListener {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void save(Object event) {

        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outbox = new OutboxEvent(
                    UUID.randomUUID(),
                    event.getClass().getSimpleName(),
                    event.getClass().getName(),
                    payload
            );

            outboxRepository.save(outbox);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Could not serialize outbox event",
                    e
            );
        }
    }

    @EventListener
    public void handleEmailContentUpdated(OutboxEmailContentUpdatedEvent outboxEmailContentUpdatedEvent){
        this.save(outboxEmailContentUpdatedEvent.emailContentUpdatedEvent());
    }

    @EventListener
    public void handleEmailCampaignSend(OutboxEmailCampaignSendEvent outboxEmailCampaignSendEvent){
        this.save(outboxEmailCampaignSendEvent.emailCampaignSendEvent());
    }
}
