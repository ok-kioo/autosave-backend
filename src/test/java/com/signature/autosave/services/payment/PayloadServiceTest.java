package com.signature.autosave.services.payment;

import com.signature.autosave.modules.contract.domain.entity.PlanContract;
import com.signature.autosave.modules.contract.domain.repository.PlanContractRepository;
import com.signature.autosave.modules.payment.payload.domain.entity.Payload;
import com.signature.autosave.modules.payment.payload.domain.enums.PayloadType;
import com.signature.autosave.modules.payment.payload.domain.repository.PayloadRepository;
import com.signature.autosave.modules.payment.payload.dto.PayloadResponseDTO;
import com.signature.autosave.modules.payment.payload.service.PayloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PayloadServiceTest {
	@Mock
	private PayloadRepository payloadRepository;
	@Mock
	private PlanContractRepository planContractRepository;
	@Mock
	private ApplicationEventPublisher publisher;
	@InjectMocks
	private PayloadService payloadService;

	@Test
	void listPayloadShouldReturnPayloadWhenFound() {
		UUID id = UUID.randomUUID();
		PlanContract contract = new PlanContract();
		contract.setId(UUID.randomUUID());
		Payload payload = buildPayload(id, 1010L, new BigDecimal("150.50"), PayloadType.PAYMENT, contract);

		when(payloadRepository.findById(id)).thenReturn(Optional.of(payload));

		PayloadResponseDTO response = payloadService.listPayload(id);

		assertEquals(id, response.id());
		assertEquals(1010L, response.paymentId());
		assertEquals(new BigDecimal("150.50"), response.amount());
		assertEquals(PayloadType.PAYMENT, response.payloadType());
	}

	@Test
	void listPayloadShouldThrowWhenNotFound() {
		UUID id = UUID.randomUUID();
		when(payloadRepository.findById(id)).thenReturn(Optional.empty());

		RuntimeException ex = assertThrows(RuntimeException.class, () -> payloadService.listPayload(id));

		assertEquals("Payload not found with id: " + id + ".", ex.getMessage());
	}

	@Test
	void listPayloadsShouldReturnMappedPage() {
		Pageable pageable = PageRequest.of(0, 10);
		PlanContract contract = new PlanContract();
		contract.setId(UUID.randomUUID());
		Payload payload1 = buildPayload(UUID.randomUUID(), 111L, new BigDecimal("10.00"), PayloadType.PAYMENT, contract);
		Payload payload2 = buildPayload(UUID.randomUUID(), 222L, new BigDecimal("20.00"), PayloadType.REFUND, contract);
		Page<Payload> page = new PageImpl<>(List.of(payload1, payload2), pageable, 2);

		when(payloadRepository.findAll(pageable)).thenReturn(page);

		Page<PayloadResponseDTO> response = payloadService.listPayloads(pageable);

		assertEquals(2, response.getTotalElements());
		assertEquals(PayloadType.PAYMENT, response.getContent().get(0).payloadType());
		assertEquals(PayloadType.REFUND, response.getContent().get(1).payloadType());
	}

	@Test
	void processPayloadShouldIgnoreUnsupportedType() throws Exception {
		Map<String, Object> webhookPayload = Map.of("type", "subscription");

		payloadService.processPayload(webhookPayload);

		verifyNoInteractions(payloadRepository);
		verifyNoInteractions(planContractRepository);
		verifyNoInteractions(publisher);
	}

	private Payload buildPayload(UUID id, long paymentId, BigDecimal amount, PayloadType payloadType, PlanContract contract) {
		Payload payload = new Payload();
		payload.setId(id);
		payload.setPaymentId(paymentId);
		payload.setAmount(amount);
		payload.setType(payloadType);
		payload.setPlanContract(contract);
		return payload;
	}
}
