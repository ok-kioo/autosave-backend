package com.signature.autosave.modules.paymentmethod.domain.repository;

import com.signature.autosave.modules.paymentmethod.domain.entity.CreditCardPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditCardPaymentMethodRepository extends JpaRepository<CreditCardPaymentMethod, UUID> {
    Optional<CreditCardPaymentMethod> findByToken(String token);

}
