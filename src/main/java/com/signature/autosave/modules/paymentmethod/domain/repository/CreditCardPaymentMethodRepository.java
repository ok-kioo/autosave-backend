package com.signature.autosave.modules.paymentmethod.domain.repository;

import com.signature.autosave.modules.paymentmethod.domain.entity.CreditCardPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface CreditCardPaymentMethodRepository extends JpaRepository<CreditCardPaymentMethod, UUID> {
    @Modifying
    @Transactional
    @Query("UPDATE CreditCardPaymentMethod c SET c.isDefault = false WHERE c.id = :id")
    void setPaymentMethodAsNonDefault(@Param("id") UUID id);
    Optional<CreditCardPaymentMethod> findByToken(String token);

}
