package com.signature.autosave.modules.pay.payment.domain.repository;

import com.signature.autosave.modules.pay.payment.domain.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    @Query("SELECT p FROM PaymentEntity p JOIN p.paymentMethod pm WHERE pm.user.id = :userId")
    List<PaymentEntity> findAllByUserId(@Param("userId") UUID userId);

}
