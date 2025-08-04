package com.signature.autosave.web.controller;

import com.signature.autosave.modules.paymentmethod.dto.PaymentMethodResponseDTO;
import com.signature.autosave.modules.paymentmethod.dto.RegisterPaymentMethodDTO;
import com.signature.autosave.modules.paymentmethod.service.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payment/methods")
public class PaymentMethodController {
    private final PaymentMethodService paymentMethodService;

    @PostMapping("/register")
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

    @GetMapping("")
    public ResponseEntity<?> listPaymentMethods(@AuthenticationPrincipal UserDetails userDetails) {

        try{
            List<PaymentMethodResponseDTO> result = paymentMethodService.listPaymentMethods(userDetails);

            return ResponseEntity.status(HttpStatus.FOUND).body(result);

        }catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deletePaymentMethod(@PathVariable("id") String id,
                                                 @AuthenticationPrincipal UserDetails userDetails) {

        try{
            paymentMethodService.deletePaymentMethod(id, userDetails);

            return ResponseEntity.ok("Método de pagamento excluído");

        }catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
        }
    }
}
