package com.signature.autosave.modules.paymentmethod.domain.repository;

import com.signature.autosave.modules.paymentmethod.domain.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    List<PaymentMethod> findAllByUserId(UUID userId);

}
