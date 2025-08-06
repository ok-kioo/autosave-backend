package com.signature.autosave.modules.paymentmethod.service;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.customer.CustomerCard;
import com.signature.autosave.infra.components.intermediation.MPComponent;
import com.signature.autosave.modules.paymentmethod.builder.CreditCardPaymentMethodBuilder;
import com.signature.autosave.modules.paymentmethod.builder.PaymentMethodBuilder;
import com.signature.autosave.modules.paymentmethod.builder.PixPaymentMethodBuilder;
import com.signature.autosave.modules.paymentmethod.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.entity.PaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.entity.PixPaymentMethod;
import com.signature.autosave.modules.paymentmethod.domain.repository.CreditCardPaymentMethodRepository;
import com.signature.autosave.modules.paymentmethod.domain.repository.PaymentMethodRepository;
import com.signature.autosave.modules.paymentmethod.domain.repository.PixPaymentMethodRepository;
import com.signature.autosave.modules.paymentmethod.dto.*;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CreditCardPaymentMethodRepository creditMethodRepository;
    private final PixPaymentMethodRepository pixMethodRepository;
    private final MPComponent mpComponent;

    @Transactional
    public PaymentMethodResponseDTO createPaymentMethod(RegisterPaymentMethodDTO registerPaymentMethod, UserDetails userDetails) throws MPException, MPApiException {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (registerPaymentMethod.isDefault()) {
            paymentMethodRepository.setPaymentMethodAsNonDefault(user.getId());
        }

        return switch (registerPaymentMethod.getType()) {
            case CREDIT_CARD -> handleCreditCardCreation(registerPaymentMethod, user);
            case PIX -> handlePixCreation(registerPaymentMethod, user);
        };
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponseDTO> listPaymentMethods(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return paymentMethodRepository.findAllByUserId(user.getId())
                .stream()
                .map(method -> responseDTOBuild(method, user))
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentMethodResponseDTO listPaymentMethod(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

        return responseDTOBuild(paymentMethod, user);
    }

    public PaymentMethodResponseDTO updatePaymentMethod(UUID id, UpdatePaymentMethodDTO updatePaymentMethodDTO, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

        if (!paymentMethod.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Você não tem permissão para atualizar este método de pagamento");
        }

        if (updatePaymentMethodDTO.isDefault()) {
            paymentMethodRepository.setPaymentMethodAsNonDefault(user.getId());
            paymentMethod.setDefault(true);
        }

        paymentMethodRepository.save(paymentMethod);

        return responseDTOBuild(paymentMethod, user);
    }

    public void deletePaymentMethod(UUID id, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

        if (!paymentMethod.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Você não tem permissão para excluir este método de pagamento");
        }

        if (paymentMethod.isDefault()) {
            throw new RuntimeException("Você não pode excluir o método de pagamento padrão");
        }

        switch (paymentMethod.getType()) {
            case CREDIT_CARD -> paymentMethodRepository.delete(creditMethodRepository.findById(paymentMethod.getId())
                    .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado")));

            case PIX -> paymentMethodRepository.delete(pixMethodRepository.findById(paymentMethod.getId())
                    .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado")));
            default -> throw new RuntimeException("Tipo de método de pagamento não suportado");
        }
    }

    private CreditCardResponseDTO handleCreditCardCreation(RegisterPaymentMethodDTO registerPaymentMethodDTO, User user) throws MPException, MPApiException {
        CustomerCard customerCard = mpComponent.savePaymentMethod(registerPaymentMethodDTO);

        CreditCardPaymentMethod creditCard = CreditCardPaymentMethodBuilder.builder()
                .withBase(basePaymentMethodBuild(registerPaymentMethodDTO, user))
                .withCustomerCard(customerCard)
                .build();

        paymentMethodRepository.save(creditCard);

        return new CreditCardResponseDTO(
                creditCard.getId(),
                creditCard.getType(),
                creditCard.getFirstName(),
                creditCard.getLastName(),
                creditCard.getEmail(),
                creditCard.getDocumentNumber(),
                creditCard.getCreatedAt(),
                creditCard.isDefault(),
                user,
                customerCard.getCardholder().getName(),
                customerCard.getIssuer().getName(),
                customerCard.getLastFourDigits());
    }

    private PixResponseDTO handlePixCreation(RegisterPaymentMethodDTO registerPaymentMethodDTO, User user) {
        PixPaymentMethod pix = PixPaymentMethodBuilder.builder()
                        .withBase(basePaymentMethodBuild(registerPaymentMethodDTO, user))
                        /*.withPixKey(registerPaymentMethodDTO.getPixKey())*/
                        .build();

        paymentMethodRepository.save(pix);

        return new PixResponseDTO(
                pix.getId(),
                pix.getType(),
                pix.getFirstName(),
                pix.getLastName(),
                pix.getEmail(),
                pix.getDocumentNumber(),
                pix.getCreatedAt(),
                pix.isDefault(),
                user);
    }

    private PaymentMethodResponseDTO responseDTOBuild(PaymentMethod paymentMethod, User user) {
        if (paymentMethod instanceof CreditCardPaymentMethod creditCard && paymentMethod.getUser().getId() == user.getId()) {
            return new CreditCardResponseDTO(
                    creditCard.getId(),
                    creditCard.getType(),
                    creditCard.getFirstName(),
                    creditCard.getLastName(),
                    creditCard.getEmail(),
                    creditCard.getDocumentNumber(),
                    creditCard.getCreatedAt(),
                    creditCard.isDefault(),
                    user,
                    creditCard.getCustomerCard().getCardholder().getName(),
                    creditCard.getCustomerCard().getIssuer().getName(),
                    creditCard.getCustomerCard().getLastFourDigits());
        } else if (paymentMethod instanceof PixPaymentMethod pix && paymentMethod.getUser().getId() == user.getId()) {
            return new PixResponseDTO(
                    pix.getId(),
                    pix.getType(),
                    pix.getFirstName(),
                    pix.getLastName(),
                    pix.getEmail(),
                    pix.getDocumentNumber(),
                    pix.getCreatedAt(),
                    pix.isDefault(),
                    user);
        }

        throw new RuntimeException("Método de pagamento não encontrado ou você não tem permissão para acessá-lo");
    }

    private PaymentMethod basePaymentMethodBuild(RegisterPaymentMethodDTO registerPaymentMethodDTO, User user) {
        return PaymentMethodBuilder.builder()
                .withType(registerPaymentMethodDTO.getType())
                .withFirstName(registerPaymentMethodDTO.getFirstName())
                .withLastName(registerPaymentMethodDTO.getLastName())
                .withEmail(registerPaymentMethodDTO.getEmail())
                .withDocumentNumber(registerPaymentMethodDTO.getDocumentNumber())
                .withCreatedAt(LocalDateTime.now())
                .withIsDefault(registerPaymentMethodDTO.isDefault())
                .withUser(user).build();
    }
}
