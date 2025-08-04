package com.signature.autosave.modules.paymentmethod.service;

import com.signature.autosave.modules.paymentmethod.builder.CreditCardPaymentMethodBuilder;
import com.signature.autosave.modules.paymentmethod.builder.PaymentMethodBuilder;
import com.signature.autosave.modules.paymentmethod.builder.PixPaymentMethodBuilder;
import com.signature.autosave.modules.paymentmethod.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.entity.PaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.entity.PixPaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.paymentmethod.domain.repository.CreditCardPaymentMethodRepository;
import com.signature.autosave.modules.paymentmethod.domain.repository.PaymentMethodRepository;
import com.signature.autosave.modules.paymentmethod.dto.CreditCardResponseDTO;
import com.signature.autosave.modules.paymentmethod.dto.PaymentMethodResponseDTO;
import com.signature.autosave.modules.paymentmethod.dto.PixResponseDTO;
import com.signature.autosave.modules.paymentmethod.dto.RegisterPaymentMethodDTO;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CreditCardPaymentMethodRepository creditMethod;

    @Transactional
    public PaymentMethodResponseDTO createPaymentMethod(RegisterPaymentMethodDTO registerPaymentMethod, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (registerPaymentMethod.isDefault()) {
            paymentMethodRepository.findAllByUserId(user.getId())
                    .forEach(paymentMethod -> {
                        if (paymentMethod instanceof CreditCardPaymentMethod) {
                            creditMethod.setPaymentMethodAsNonDefault(paymentMethod.getId());
                        }
                    });
        }

        return switch (registerPaymentMethod.getType()) {
            case CREDIT_CARD -> handleCreditCardCreation(registerPaymentMethod, user);
            case PIX -> handlePixCreation(registerPaymentMethod, user);
        };
    }

    public void deletePaymentMethod(String id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

        if (!paymentMethod.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Você não tem permissão para excluir este método de pagamento");
        }

        creditMethod.findById(paymentMethod.getId()).ifPresent(creditCardPaymentMethod -> {

            if (creditCardPaymentMethod.isDefault()) {
                throw new RuntimeException("Você não pode excluir o método de pagamento padrão");
            }
            paymentMethodRepository.delete(creditCardPaymentMethod);

        });

    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponseDTO> listPaymentMethods(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return paymentMethodRepository.findAllByUserId(user.getId())
                .stream()
                .map(method -> {
                    if (method instanceof CreditCardPaymentMethod) {
                        CreditCardPaymentMethod creditCard = (CreditCardPaymentMethod) method;
                        return new CreditCardResponseDTO(
                                creditCard.getId(),
                                creditCard.getType(),
                                creditCard.getToken(),
                                creditCard.getCardHolderName(),
                                creditCard.getLastFourDigits(),
                                creditCard.isDefault(),
                                creditCard.getCreatedAt());
                    } else if (method.getType() == PaymentMethodType.PIX) {
                        PixPaymentMethod pix = (PixPaymentMethod) method;
                        return new PixResponseDTO(
                                pix.getId(),
                                pix.getType(),
                                pix.getPixKey(),
                                pix.getCreatedAt());
                    }
                    return null;
                })
                .toList();
    }


    private CreditCardResponseDTO handleCreditCardCreation(RegisterPaymentMethodDTO dto, User user) {
        creditMethod.findByToken(dto.getToken())
                .ifPresent(existingCard -> {
                    throw new RuntimeException("Esse cartão já foi registrado");
                });

        CreditCardPaymentMethod creditCard = CreditCardPaymentMethodBuilder.builder()
                .withBase(basePaymentMethodBuild(dto, user))
                .withToken(dto.getToken())
                .withCardHolderName(dto.getCardHolderName())
                .withLastFourDigits(dto.getLastFourDigits())
                .build();

        creditCard.setToken(dto.getToken());
        creditCard.setCardHolderName(dto.getCardHolderName());
        creditCard.setLastFourDigits(dto.getLastFourDigits());
        creditCard.setDefault(dto.isDefault());

        paymentMethodRepository.save(creditCard);

        return new CreditCardResponseDTO(
                creditCard.getId(),
                creditCard.getType(),
                creditCard.getToken(),
                creditCard.getCardHolderName(),
                creditCard.getLastFourDigits(),
                creditCard.isDefault(),
                creditCard.getCreatedAt());
    }

    private PixResponseDTO handlePixCreation(RegisterPaymentMethodDTO dto, User user) {
        PixPaymentMethod pix = PixPaymentMethodBuilder.builder()
                        .withBase(basePaymentMethodBuild(dto, user))
                        .withPixKey(dto.getPixKey())
                        .build();

        paymentMethodRepository.save(pix);

        return new PixResponseDTO(
                pix.getId(),
                pix.getType(),
                pix.getPixKey(),
                pix.getCreatedAt());
    }

    private PaymentMethod basePaymentMethodBuild(RegisterPaymentMethodDTO dto, User user) {
        return PaymentMethodBuilder.builder()
                .withType(dto.getType())
                .withCreatedAt(LocalDate.now())
                .withUser(user).build();
    }
}
