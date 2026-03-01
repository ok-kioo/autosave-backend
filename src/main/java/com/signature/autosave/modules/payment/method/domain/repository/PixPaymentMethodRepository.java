package com.signature.autosave.modules.payment.method.domain.repository;

import com.signature.autosave.modules.payment.method.domain.entity.PixPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PixPaymentMethodRepository extends JpaRepository<PixPaymentMethod, UUID> {
}
