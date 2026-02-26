package com.signature.autosave.modules.payment.domain.repository;

import com.signature.autosave.modules.payment.domain.entity.PixPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PixPaymentMethodRepository extends JpaRepository<PixPaymentMethod, UUID> {
}
