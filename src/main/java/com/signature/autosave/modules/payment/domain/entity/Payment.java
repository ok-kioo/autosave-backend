package com.signature.autosave.modules.payment.domain.entity;

import com.signature.autosave.modules.paymentmethod.domain.entity.PaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @ManyToOne
    @JoinColumn(name = "payment_method", referencedColumnName = "id")
    private PaymentMethod paymentMethod;

    @NotBlank
    private PaymentStatus paymentStatus;

    @NotBlank
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    @NotBlank
    private LocalDateTime ExpiresAt;

}
