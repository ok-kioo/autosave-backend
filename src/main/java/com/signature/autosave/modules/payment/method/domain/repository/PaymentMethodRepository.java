package com.signature.autosave.modules.payment.method.domain.repository;

import com.signature.autosave.modules.payment.method.domain.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    @Modifying
    @Transactional
    @Query("UPDATE PaymentMethod p SET p.isDefault = false WHERE p.user.id = :userId")
    void setPaymentMethodAsNonDefault(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentMethod p SET p.isActive = false WHERE p.id = :id")
    void setPaymentMethodAsNonActive(@Param("id") UUID id);

    Optional<PaymentMethod> findByIdAndIsActiveTrue(UUID id);

    @Query("SELECT p FROM PaymentMethod p WHERE p.user.id = :userId AND p.isActive = true")
    List<PaymentMethod> findAllByUserIdAndIsActiveTrue(@Param("userId") UUID userId);

}
