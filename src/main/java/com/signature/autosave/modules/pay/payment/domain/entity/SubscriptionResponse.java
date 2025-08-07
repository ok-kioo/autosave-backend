package com.signature.autosave.modules.pay.payment.domain.entity;

import com.signature.autosave.infra.components.converter.SubscriptionConverter;
import com.signature.autosave.modules.pay.payment.dto.Subscription;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscription_response")
@Getter
@Setter
public class SubscriptionResponse extends PaymentEntity {
    @NotBlank
    @Lob
    @Convert(converter = SubscriptionConverter.class)
    private Subscription subscriptionResponse;
}
