package com.signature.autosave.modules.payment.method.service;

import com.mercadopago.client.customer.CustomerCardClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.customer.Customer;
import com.mercadopago.resources.customer.CustomerCard;
import com.signature.autosave.infra.components.intermediation.IIntermediationComponent;
import com.signature.autosave.modules.payment.method.builder.CreditCardPaymentMethodBuilder;
import com.signature.autosave.modules.payment.method.builder.PaymentMethodBuilder;
import com.signature.autosave.modules.payment.method.builder.PixPaymentMethodBuilder;
import com.signature.autosave.modules.payment.method.domain.entity.CreditCardPaymentMethod;
import com.signature.autosave.modules.payment.method.domain.entity.PaymentMethod;
import com.signature.autosave.modules.payment.method.domain.entity.PixPaymentMethod;
import com.signature.autosave.modules.payment.method.domain.repository.CreditCardPaymentMethodRepository;
import com.signature.autosave.modules.payment.method.domain.repository.PaymentMethodRepository;
import com.signature.autosave.modules.payment.method.domain.repository.PixPaymentMethodRepository;
import com.signature.autosave.modules.payment.method.dto.*;
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
    private final IIntermediationComponent mpComponent;

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

        return paymentMethodRepository.findAllByUserIdAndIsActiveTrue(user.getId())
                .stream()
                .map(method -> {
                    try {
                        return responseDTOBuild(method, user);
                    } catch (MPException | MPApiException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentMethodResponseDTO listPaymentMethod(UUID id, UserDetails userDetails) throws MPException, MPApiException {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

        return responseDTOBuild(paymentMethod, user);
    }

    public PaymentMethodResponseDTO updatePaymentMethod(UUID id, UpdatePaymentMethodDTO updatePaymentMethodDTO, UserDetails userDetails) throws MPException, MPApiException {
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

    public void deletePaymentMethod(UUID id, UserDetails userDetails) throws MPException, MPApiException {
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

        if(paymentMethod instanceof CreditCardPaymentMethod creditCardPaymentMethod){
            creditMethodRepository.findById(paymentMethod.getId())
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

            paymentMethodRepository.setPaymentMethodAsNonActive(paymentMethod.getId());

            //new CustomerCardClient().delete(creditCardPaymentMethod.getCustomerId(), creditCardPaymentMethod.getCustomerCardId());
        }

        if(paymentMethod instanceof PixPaymentMethod) {
            pixMethodRepository.findById(paymentMethod.getId())
                    .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

            paymentMethodRepository.setPaymentMethodAsNonActive(paymentMethod.getId());
        }
    }

    private CreditCardResponseDTO handleCreditCardCreation(RegisterPaymentMethodDTO registerPaymentMethodDTO, User user) throws MPException, MPApiException {
        if(registerPaymentMethodDTO.getGatewayPaymentMethodId() == null
            || registerPaymentMethodDTO.getGatewayToken() == null
            || registerPaymentMethodDTO.getGatewayIssuerId() == null) {
            throw new RuntimeException("É necessário cadastrar o cartão com os campos token, issuerId e paymentMethodId preenchidos");
        }

        Customer customer = mpComponent.createCustomer(registerPaymentMethodDTO);
        CustomerCard customerCard = mpComponent.saveCreditCard(registerPaymentMethodDTO, customer);

        CreditCardPaymentMethod creditCard = CreditCardPaymentMethodBuilder.builder()
                .withBase(basePaymentMethodBuild(registerPaymentMethodDTO, user))
                .withCustomerCard(customerCard.getId())
                .withCustomer(customer.getId())
                .build();

        paymentMethodRepository.save(creditCard);

        return new CreditCardResponseDTO(
                creditCard.getId(),
                creditCard.getType(),
                creditCard.getFirstName(),
                creditCard.getLastName(),
                creditCard.getDocumentNumber(),
                creditCard.getCreatedAt(),
                creditCard.isDefault(),
                user,
                customerCard.getCardholder().getName(),
                customerCard.getIssuer().getName(),
                customerCard.getLastFourDigits());
    }

    private PixResponseDTO handlePixCreation(RegisterPaymentMethodDTO registerPaymentMethodDTO, User user) throws MPException, MPApiException {
        Customer customer = mpComponent.createCustomer(registerPaymentMethodDTO);

        PixPaymentMethod pix = PixPaymentMethodBuilder.builder()
                        .withBase(basePaymentMethodBuild(registerPaymentMethodDTO, user))
                        .withCustomer(customer.getId())
                        .build();

        paymentMethodRepository.save(pix);

        return new PixResponseDTO(
                pix.getId(),
                pix.getType(),
                pix.getFirstName(),
                pix.getLastName(),
                pix.getDocumentNumber(),
                pix.getCreatedAt(),
                pix.isDefault(),
                user);
    }

    private PaymentMethodResponseDTO responseDTOBuild(PaymentMethod paymentMethod, User user) throws MPException, MPApiException {
        if (paymentMethod instanceof CreditCardPaymentMethod creditCardPaymentMethod && paymentMethod.getUser().getId() == user.getId()) {
            CustomerCardClient client = new CustomerCardClient();
            CustomerCard card = client.get(creditCardPaymentMethod.getCustomerId(), creditCardPaymentMethod.getCustomerCardId());

            return new CreditCardResponseDTO(
                    creditCardPaymentMethod.getId(),
                    creditCardPaymentMethod.getType(),
                    creditCardPaymentMethod.getFirstName(),
                    creditCardPaymentMethod.getLastName(),
                    creditCardPaymentMethod.getDocumentNumber(),
                    creditCardPaymentMethod.getCreatedAt(),
                    creditCardPaymentMethod.isDefault(),
                    user,
                    card.getCardholder().getName(),
                    card.getIssuer().getName(),
                    card.getLastFourDigits());
        } else if (paymentMethod instanceof PixPaymentMethod pix && paymentMethod.getUser().getId() == user.getId()) {
            return new PixResponseDTO(
                    pix.getId(),
                    pix.getType(),
                    pix.getFirstName(),
                    pix.getLastName(),
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
