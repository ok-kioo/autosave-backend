package com.signature.autosave.modules.pay.paymentmethod.domain.repository;

import com.signature.autosave.modules.pay.paymentmethod.domain.entity.CreditCardPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditCardPaymentMethodRepository extends JpaRepository<CreditCardPaymentMethod, UUID> {

}
