package com.signature.autosave.modules.payment.domain.entity;

import com.signature.autosave.modules.payment.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String documentNumber;

    @NotBlank
    private LocalDateTime createdAt;

    @Column(name = "is_default")
    private boolean isDefault;

    @NotBlank
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

}


