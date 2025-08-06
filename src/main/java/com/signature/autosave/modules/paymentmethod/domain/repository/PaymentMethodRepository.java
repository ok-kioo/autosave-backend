package com.signature.autosave.modules.paymentmethod.domain.repository;

import com.signature.autosave.modules.paymentmethod.domain.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    @Modifying
    @Transactional
    @Query("UPDATE PaymentMethod p SET p.isDefault = false WHERE p.user.id = :id")
    void setPaymentMethodAsNonDefault(@Param("id") UUID id);
    List<PaymentMethod> findAllByUserId(UUID userId);

}
