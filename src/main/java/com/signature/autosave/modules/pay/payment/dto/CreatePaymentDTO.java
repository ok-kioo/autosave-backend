package com.signature.autosave.modules.pay.payment.dto;

import com.signature.autosave.modules.pay.paymentmethod.domain.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreatePaymentDTO {
    @NotBlank
    private PaymentMethod paymentMethod;

    private int installments;

    @NotBlank
    private String securityCode;


}
