package com.signature.autosave.modules.contract.domain.repository;

import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PlanContractRepository extends JpaRepository<PlanContract, UUID> {
    @Query("SELECT p FROM PlanContract p JOIN p.paymentMethod pm WHERE pm.user.id = :userId")
    List<PlanContract> findAllByUserId(@Param("userId") UUID userId);

}
