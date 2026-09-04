package com.signature.autosave.services.payment;

import com.mercadopago.resources.customer.Customer;
import com.signature.autosave.infra.components.intermediation.IGatewayComponent;
import com.signature.autosave.modules.payment.method.domain.entity.PaymentMethod;
import com.signature.autosave.modules.payment.method.domain.entity.PixPaymentMethod;
import com.signature.autosave.modules.payment.method.domain.enums.PaymentMethodType;
import com.signature.autosave.modules.payment.method.domain.repository.CreditCardPaymentMethodRepository;
import com.signature.autosave.modules.payment.method.domain.repository.PaymentMethodRepository;
import com.signature.autosave.modules.payment.method.domain.repository.PixPaymentMethodRepository;
import com.signature.autosave.modules.payment.method.dto.PaymentMethodResponseDTO;
import com.signature.autosave.modules.payment.method.dto.RegisterPaymentMethodDTO;
import com.signature.autosave.modules.payment.method.dto.UpdatePaymentMethodDTO;
import com.signature.autosave.modules.payment.method.service.PaymentMethodService;
import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.enums.Role;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import com.signature.autosave.modules.user.service.events.UserDeletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentMethodServiceTest {
	@Mock
	private UserRepository userRepository;
	@Mock
	private PaymentMethodRepository paymentMethodRepository;
	@Mock
	private CreditCardPaymentMethodRepository creditMethodRepository;
	@Mock
	private PixPaymentMethodRepository pixMethodRepository;
	@Mock
	private IGatewayComponent gatewayComponent;
	@Mock
	private UserDetails userDetails;
	@InjectMocks
	private PaymentMethodService paymentMethodService;

	@Test
	void createPaymentMethodShouldCreatePixMethodAndReturnResponse() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID paymentId = UUID.randomUUID();
		User user = buildUser(userId, "Kio", "kio@mail.com");
		RegisterPaymentMethodDTO dto = pixRegisterDTO(true);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(gatewayComponent.createCustomer(dto)).thenReturn(org.mockito.Mockito.mock(Customer.class));
		doAnswer(invocation -> {
			PixPaymentMethod method = invocation.getArgument(0);
			method.setId(paymentId);
			method.setCreatedAt(LocalDateTime.now());
			return method;
		}).when(paymentMethodRepository).save(any(PaymentMethod.class));

		PaymentMethodResponseDTO response = paymentMethodService.createPaymentMethod(dto, userDetails);

		assertEquals(paymentId, response.getId());
		assertEquals(PaymentMethodType.PIX, response.getType());
		assertEquals(userId, response.getUser().getId());
		verify(paymentMethodRepository).setPaymentMethodAsNonDefault(userId);
	}

	@Test
	void listPaymentMethodsShouldReturnMappedPageForPix() {
		UUID userId = UUID.randomUUID();
		User user = buildUser(userId, "Kio", "kio@mail.com");
		Pageable pageable = PageRequest.of(0, 10);
		PixPaymentMethod method = buildPixMethod(UUID.randomUUID(), user, true);
		Page<PaymentMethod> page = new PageImpl<>(List.of(method), pageable, 1);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(paymentMethodRepository.findAllByUserIdAndIsActiveTrue(userId, pageable)).thenReturn(page);

		Page<PaymentMethodResponseDTO> response = paymentMethodService.listPaymentMethods(userDetails, pageable);

		assertEquals(1, response.getTotalElements());
		assertEquals(PaymentMethodType.PIX, response.getContent().get(0).getType());
	}

	@Test
	void updatePaymentMethodShouldSetAsDefaultWhenRequested() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID methodId = UUID.randomUUID();
		User user = buildUser(userId, "Kio", "kio@mail.com");
		PixPaymentMethod method = buildPixMethod(methodId, user, false);
		UpdatePaymentMethodDTO dto = new UpdatePaymentMethodDTO();
		ReflectionTestUtils.setField(dto, "isDefault", true);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(paymentMethodRepository.findById(methodId)).thenReturn(Optional.of(method));

		PaymentMethodResponseDTO response = paymentMethodService.updatePaymentMethod(methodId, dto, userDetails);

		assertEquals(true, response.isDefault());
		verify(paymentMethodRepository).setPaymentMethodAsNonDefault(userId);
		verify(paymentMethodRepository).save(method);
	}

	@Test
	void updatePaymentMethodShouldThrowWhenUserHasNoPermission() {
		UUID userId = UUID.randomUUID();
		UUID otherUserId = UUID.randomUUID();
		UUID methodId = UUID.randomUUID();
		User user = buildUser(userId, "Kio", "kio@mail.com");
		User otherUser = buildUser(otherUserId, "Other", "other@mail.com");
		PixPaymentMethod method = buildPixMethod(methodId, otherUser, false);
		UpdatePaymentMethodDTO dto = new UpdatePaymentMethodDTO();
		ReflectionTestUtils.setField(dto, "isDefault", true);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(paymentMethodRepository.findById(methodId)).thenReturn(Optional.of(method));

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> paymentMethodService.updatePaymentMethod(methodId, dto, userDetails));

		assertEquals("You do not have permission to update this payment method.", ex.getMessage());
		verify(paymentMethodRepository, never()).save(any(PaymentMethod.class));
	}

	@Test
	void deletePaymentMethodShouldDisablePixMethod() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID methodId = UUID.randomUUID();
		User user = buildUser(userId, "Kio", "kio@mail.com");
		PixPaymentMethod method = buildPixMethod(methodId, user, false);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(paymentMethodRepository.findById(methodId)).thenReturn(Optional.of(method));
		when(pixMethodRepository.findById(methodId)).thenReturn(Optional.of(method));

		paymentMethodService.deletePaymentMethod(methodId, userDetails);

		verify(paymentMethodRepository).setPaymentMethodAsNonActive(methodId);
	}

	@Test
	void deletePaymentMethodShouldThrowWhenDefaultMethod() {
		UUID userId = UUID.randomUUID();
		UUID methodId = UUID.randomUUID();
		User user = buildUser(userId, "Kio", "kio@mail.com");
		PixPaymentMethod method = buildPixMethod(methodId, user, true);

		when(userDetails.getUsername()).thenReturn("kio@mail.com");
		when(userRepository.findByEmailAndIsActiveTrue("kio@mail.com")).thenReturn(Optional.of(user));
		when(paymentMethodRepository.findById(methodId)).thenReturn(Optional.of(method));

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> paymentMethodService.deletePaymentMethod(methodId, userDetails));

		assertEquals("You can not delete your default payment method.", ex.getMessage());
		verify(paymentMethodRepository, never()).setPaymentMethodAsNonActive(any(UUID.class));
	}

	@Test
	void cascadeDeletePaymentMethodsShouldDisableAllUserMethods() {
		UUID userId = UUID.randomUUID();
		UUID method1 = UUID.randomUUID();
		UUID method2 = UUID.randomUUID();
		User user = buildUser(userId, "Kio", "kio@mail.com");
		PixPaymentMethod pix1 = buildPixMethod(method1, user, false);
		PixPaymentMethod pix2 = buildPixMethod(method2, user, false);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(paymentMethodRepository.findAllByUserIdAndIsActiveTrue(userId)).thenReturn(List.of(pix1, pix2));

		paymentMethodService.cascadeDeletePaymentMethods(new UserDeletedEvent(userId));

		verify(paymentMethodRepository).setPaymentMethodAsNonActive(method1);
		verify(paymentMethodRepository).setPaymentMethodAsNonActive(method2);
	}

	private RegisterPaymentMethodDTO pixRegisterDTO(boolean isDefault) {
		RegisterPaymentMethodDTO dto = new RegisterPaymentMethodDTO();
		ReflectionTestUtils.setField(dto, "firstName", "Kio");
		ReflectionTestUtils.setField(dto, "lastName", "Silva");
		ReflectionTestUtils.setField(dto, "email", "kio@mail.com");
		ReflectionTestUtils.setField(dto, "documentNumber", "12345678900");
		ReflectionTestUtils.setField(dto, "type", PaymentMethodType.PIX);
		ReflectionTestUtils.setField(dto, "isDefault", isDefault);
		return dto;
	}

	private User buildUser(UUID id, String name, String email) {
		User user = new User();
		user.setId(id);
		user.setName(name);
		user.setEmail(email);
		user.setPassword("encoded-password");
		user.setRole(Role.VIEWER);
		user.setActive(true);
		return user;
	}

	private PixPaymentMethod buildPixMethod(UUID id, User user, boolean isDefault) {
		PixPaymentMethod pix = new PixPaymentMethod();
		pix.setId(id);
		pix.setType(PaymentMethodType.PIX);
		pix.setFirstName("Kio");
		pix.setLastName("Silva");
		pix.setDocumentNumber("12345678900");
		pix.setCreatedAt(LocalDateTime.now());
		pix.setDefault(isDefault);
		pix.setActive(true);
		pix.setUser(user);
		pix.setCustomerId("customer-1");
		return pix;
	}
}
