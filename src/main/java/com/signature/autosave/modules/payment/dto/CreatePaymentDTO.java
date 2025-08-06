package com.signature.autosave.modules.payment.dto;

import com.signature.autosave.modules.paymentmethod.domain.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreatePaymentDTO {
    @NotBlank
    private PaymentMethod paymentMethod;

    private int installments;

    private int securityCode;


}
