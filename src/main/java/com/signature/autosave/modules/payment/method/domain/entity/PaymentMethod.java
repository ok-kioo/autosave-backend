package com.signature.autosave.modules.payment.method.domain.entity;

import com.signature.autosave.modules.payment.method.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentMethodType type;

    @NotBlank
    @Column(name = "first_name")
    private String firstName;

    @NotBlank
    @Column(name = "last_name")
    private String lastName;

    @NotBlank
    @Column(name = "document_number")
    private String documentNumber;

    @NotNull
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull
    @Column(name="is_active")
    private boolean isActive = true;

    @Column(name = "disabled_at")
    private LocalDateTime disabledAt;

    @NotNull
    @Column(name = "is_default")
    private boolean isDefault;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

}


