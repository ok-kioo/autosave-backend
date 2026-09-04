package com.signature.autosave.modules.outbox.domain.repository;

import com.signature.autosave.modules.outbox.domain.entity.OutboxEvent;
import com.signature.autosave.modules.outbox.domain.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);

}
