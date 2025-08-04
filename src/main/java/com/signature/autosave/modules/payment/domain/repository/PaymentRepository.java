package com.signature.autosave.modules.payment.domain.repository;

import com.signature.autosave.modules.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

}
