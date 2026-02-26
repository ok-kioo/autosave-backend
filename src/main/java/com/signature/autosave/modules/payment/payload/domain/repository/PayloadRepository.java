package com.signature.autosave.modules.payment.payload.domain.repository;

import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import com.signature.autosave.modules.payment.payload.domain.entity.Payload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PayloadRepository extends JpaRepository<Payload, UUID> {
    Optional<Payload> findByPlanContract(PlanContract planContract);
}
