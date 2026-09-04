package com.signature.autosave.controller.payment;

import com.signature.autosave.modules.payment.method.dto.PaymentMethodResponseDTO;
import com.signature.autosave.modules.payment.method.dto.RegisterPaymentMethodDTO;
import com.signature.autosave.modules.payment.method.dto.UpdatePaymentMethodDTO;
import com.signature.autosave.modules.payment.method.service.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment/")
public class PaymentMethodController {
    private final PaymentMethodService paymentMethodService;

    @PostMapping("methods/register")
    public ResponseEntity<?> registerPaymentMethod(@RequestBody @Valid RegisterPaymentMethodDTO registerPaymentMethodDTO,
                                                   BindingResult validation,
                                                   @AuthenticationPrincipal UserDetails userDetails) {

        if (validation.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("Error", validation.getAllErrors()));
        }

        try {
            PaymentMethodResponseDTO result = paymentMethodService.createPaymentMethod(registerPaymentMethodDTO, userDetails);
            System.out.println(result);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
        }

    }

    @GetMapping("methods")
    public ResponseEntity<?> listPaymentMethods(@AuthenticationPrincipal UserDetails userDetails,
                                                @PageableDefault(size = 10, sort = "isDefault") Pageable pageable) {

        try{
            Page<PaymentMethodResponseDTO> result = paymentMethodService.listPaymentMethods(userDetails, pageable);

            return ResponseEntity.status(HttpStatus.FOUND).body(result);

        }catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
        }
    }

    @GetMapping("methods/{id}")
    public ResponseEntity<?> listPaymentMethod(@PathVariable("id") UUID id,
                                               @AuthenticationPrincipal UserDetails userDetails) {

        try{
            PaymentMethodResponseDTO result = paymentMethodService.listPaymentMethod(id, userDetails);

            return ResponseEntity.status(HttpStatus.FOUND).body(result);

        }catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
        }
    }

    @PatchMapping("methods/update/{id}")
    public ResponseEntity<?> updatePaymentMethod(@PathVariable("id") UUID id,
                                                 @RequestBody @Valid UpdatePaymentMethodDTO updatePaymentMethodDTO,
                                                 BindingResult validation,
                                                 @AuthenticationPrincipal UserDetails userDetails) {

        if (validation.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("Error", validation.getAllErrors()));
        }

        try {
            PaymentMethodResponseDTO result = paymentMethodService.updatePaymentMethod(id, updatePaymentMethodDTO, userDetails);
            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
        }
    }

    @DeleteMapping("methods/delete/{id}")
    public ResponseEntity<?> deletePaymentMethod(@PathVariable("id") UUID id,
                                                 @AuthenticationPrincipal UserDetails userDetails) {

        try{
            paymentMethodService.deletePaymentMethod(id, userDetails);

            return ResponseEntity.ok("Método de pagamento excluído");

        }catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
        }
    }
}
