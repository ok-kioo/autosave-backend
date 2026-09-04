package com.signature.autosave.services.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signature.autosave.modules.email.campaign.service.events.EmailCampaignSendEvent;
import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import com.signature.autosave.modules.email.content.service.events.EmailContentUpdatedEvent;
import com.signature.autosave.modules.outbox.domain.entity.OutboxEvent;
import com.signature.autosave.modules.outbox.domain.enums.OutboxStatus;
import com.signature.autosave.modules.outbox.domain.repository.OutboxRepository;
import com.signature.autosave.modules.outbox.service.OutboxListener;
import com.signature.autosave.modules.outbox.service.OutboxPublisher;
import com.signature.autosave.modules.outbox.service.events.OutboxEmailContentUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {
	@Mock
	private OutboxRepository outboxRepository;
	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private ApplicationEventPublisher publisher;
	@InjectMocks
	private OutboxListener outboxListener;
	@InjectMocks
	private OutboxPublisher outboxPublisher;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(outboxPublisher, "publisher", publisher);
	}

	@Test
	void saveShouldPersistSerializedEvent() throws Exception {
		EmailContentUpdatedEvent event = new EmailContentUpdatedEvent(UUID.randomUUID(), new EmailContent(), UUID.randomUUID(), "reviewer@mail.com");
		when(objectMapper.writeValueAsString(event)).thenReturn("{\"ok\":true}");

		outboxListener.save(event);

		ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
		verify(outboxRepository).save(captor.capture());
		OutboxEvent persisted = captor.getValue();

		assertNotNull(persisted.getId());
		assertEquals("EmailContentUpdatedEvent", persisted.getAggregateType());
		assertEquals(EmailContentUpdatedEvent.class.getName(), persisted.getEventType());
		assertEquals(OutboxStatus.PENDING, persisted.getStatus());
	}

	@Test
	void saveShouldThrowWhenSerializationFails() throws Exception {
		EmailCampaignSendEvent event = new EmailCampaignSendEvent(UUID.randomUUID(), UUID.randomUUID(), new EmailContent(), "preview", List.of("a@mail.com"));
		when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("fail") {
		});

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> outboxListener.save(event));

		assertEquals("Could not serialize outbox event", ex.getMessage());
		verify(outboxRepository, never()).save(any(OutboxEvent.class));
	}

	@Test
	void handleEmailContentUpdatedShouldPersistInnerEvent() throws Exception {
		EmailContentUpdatedEvent event = new EmailContentUpdatedEvent(UUID.randomUUID(), new EmailContent(), UUID.randomUUID(), "reviewer@mail.com");
		when(objectMapper.writeValueAsString(event)).thenReturn("{\"ok\":true}");

		outboxListener.handleEmailContentUpdated(new OutboxEmailContentUpdatedEvent(event));

		verify(outboxRepository).save(any(OutboxEvent.class));
	}

	@Test
	void publishPendingEventsShouldPublishAndMarkAsPublished() throws Exception {
		OutboxEvent event1 = new OutboxEvent(UUID.randomUUID(), "EmailContentUpdatedEvent", "EmailContentUpdatedEvent", "payload-1");
		OutboxEvent event2 = new OutboxEvent(UUID.randomUUID(), "EmailCampaignSendEvent", "EmailCampaignSendEvent", "payload-2");
		EmailContentUpdatedEvent emailContentEvent =
				new EmailContentUpdatedEvent(event1.getId(), new EmailContent(), UUID.randomUUID(), "reviewer@mail.com");
		EmailCampaignSendEvent campaignSendEvent =
				new EmailCampaignSendEvent(event2.getId(), UUID.randomUUID(), new EmailContent(), "preview", List.of("a@mail.com"));

		when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(event1, event2));
		when(objectMapper.readValue("payload-1", EmailContentUpdatedEvent.class)).thenReturn(emailContentEvent);
		when(objectMapper.readValue("payload-2", EmailCampaignSendEvent.class)).thenReturn(campaignSendEvent);

		outboxPublisher.publishPendingEvents();

		verify(publisher).publishEvent(any(EmailContentUpdatedEvent.class));
		verify(publisher).publishEvent(any(EmailCampaignSendEvent.class));
		verify(outboxRepository).save(event1);
		verify(outboxRepository).save(event2);
		assertEquals(OutboxStatus.PUBLISHED, event1.getStatus());
		assertEquals(OutboxStatus.PUBLISHED, event2.getStatus());
	}

	@Test
	void publishPendingEventsShouldKeepPendingWhenProcessingFails() throws Exception {
		OutboxEvent event = new OutboxEvent(UUID.randomUUID(), "EmailContentUpdatedEvent", "EmailContentUpdatedEvent", "payload");

		when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(event));
		when(objectMapper.readValue("payload", EmailContentUpdatedEvent.class)).thenThrow(new RuntimeException("invalid payload"));

		outboxPublisher.publishPendingEvents();

		assertEquals(OutboxStatus.PENDING, event.getStatus());
		verify(outboxRepository, never()).save(event);
	}


}