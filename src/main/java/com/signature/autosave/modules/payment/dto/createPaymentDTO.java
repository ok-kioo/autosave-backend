package com.signature.autosave.modules.payment.dto;

import com.signature.autosave.modules.paymentmethod.domain.entity.PaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class createPaymentDTO {
    @NotBlank
    private PaymentMethod paymentMethod;

    @NotBlank
    private PaymentStatus status;

    @NotBlank
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    @NotBlank
    private LocalDateTime expiresAt;

}
