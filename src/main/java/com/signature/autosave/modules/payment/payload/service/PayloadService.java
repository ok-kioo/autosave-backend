package com.signature.autosave.modules.payment.payload.service;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import com.signature.autosave.modules.contract.domain.repository.PlanContractRepository;
import com.signature.autosave.modules.payment.payload.domain.entity.Payload;
import com.signature.autosave.modules.payment.payload.domain.enums.PayloadType;
import com.signature.autosave.modules.payment.payload.domain.repository.PayloadRepository;
import com.signature.autosave.modules.payment.payload.dto.PayloadResponseDTO;
import com.signature.autosave.modules.payment.payload.service.event.PayloadCreateEvent;
import com.signature.autosave.modules.payment.payload.service.event.PayloadRefundEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayloadService {
    private final PayloadRepository payloadRepository;
    private final PlanContractRepository planContractRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    public PayloadResponseDTO listPayload(UUID id){
        Payload payload = payloadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payload not found with id: " + id + "."));

        return new PayloadResponseDTO(
                payload.getId(),
                payload.getAmount(),
                payload.getPaymentId(),
                payload.getType(),
                payload.getPlanContract());
    }

    @Transactional(readOnly = true)
    public Page<PayloadResponseDTO> listPayloads(Pageable pageable) {
        return payloadRepository.findAll(pageable)
                .map(payload -> new PayloadResponseDTO(
                        payload.getId(),
                        payload.getAmount(),
                        payload.getPaymentId(),
                        payload.getType(),
                        payload.getPlanContract()
                ));
    }

    public void processPayload(Map<String, Object> payload) throws MPException, MPApiException {
        String type = (String) payload.get("type");

        if ("payment".equals(type)) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Long paymentId = Long.valueOf(data.get("id").toString());

            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(paymentId);

            this.savePayload(payment);
        }
    }

    private void savePayload(Payment payment) {
        String status = payment.getStatus();
        PlanContract planContract = planContractRepository.findById(UUID.fromString(payment.getExternalReference()))
                .orElseThrow(() -> new RuntimeException("PlanContract not found with id: " + payment.getExternalReference() + "."));

        if ("approved".equals(status)) {
            handleApprovedPayment(payment, planContract);
        }

        if ("refunded".equals(status)) {
            handleRefund(payment, planContract);
        }

    }

    @Transactional
    private void handleApprovedPayment(Payment payment, PlanContract planContract){
        Payload payload = new Payload();
        payload.setPaymentId(payment.getId());
        payload.setAmount(payment.getTransactionAmount());
        payload.setPaymentId(payment.getId());
        payload.setType(PayloadType.PAYMENT);
        payload.setPlanContract(planContract);

        payloadRepository.save(payload);

        publisher.publishEvent(new PayloadCreateEvent(planContract.getId()));
    }

    @Transactional
    private void handleRefund(Payment payment, PlanContract planContract){
        Payload payload = new Payload();
        payload.setPaymentId(payment.getId());
        payload.setAmount(payment.getTransactionAmount());
        payload.setPaymentId(payment.getId());
        payload.setType(PayloadType.REFUND);
        payload.setPlanContract(planContract);

        payloadRepository.save(payload);

        publisher.publishEvent(new PayloadRefundEvent(planContract.getId()));

    }
}
