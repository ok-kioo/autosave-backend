package com.signature.autosave.modules.outbox.domain.entity;

import com.signature.autosave.modules.outbox.domain.enums.OutboxStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Getter
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    private String aggregateType;

    @NotNull
    private String eventType;

    @Lob
    @NotNull
    private String payload;

    @NotNull
    @Setter
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @NotNull
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public OutboxEvent(UUID id, String aggregateType, String eventType, String payload) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}
