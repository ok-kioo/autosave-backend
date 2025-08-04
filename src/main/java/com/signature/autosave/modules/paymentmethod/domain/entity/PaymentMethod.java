package com.signature.autosave.modules.paymentmethod.domain.entity;

import com.signature.autosave.modules.paymentmethod.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payment_method")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class PaymentMethod {
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Enumerated(EnumType.STRING)
    private PaymentMethodType type;

    @NotBlank
    private LocalDate createdAt;

    @NotBlank
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

}


