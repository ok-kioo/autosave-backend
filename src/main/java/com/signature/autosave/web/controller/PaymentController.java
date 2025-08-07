package com.signature.autosave.web.controller;

import com.mercadopago.net.HttpStatus;
import com.signature.autosave.modules.pay.payment.dto.CreatePaymentDTO;
import com.signature.autosave.modules.pay.payment.dto.PaymentResponseDTO;
import com.signature.autosave.modules.pay.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("payments/Basic")
    public ResponseEntity<?> createPlanBasic(@RequestBody @Valid CreatePaymentDTO createPaymentDTO, BindingResult validation,
                                             @RequestHeader("X-Idempotency-Key") String idempotencyKey,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        if(validation.hasErrors()){
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            String BASIC_PLAN = "basic";
            PaymentResponseDTO result = paymentService.createPayment(createPaymentDTO, userDetails, idempotencyKey, BASIC_PLAN);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("payments/Premium")
    public ResponseEntity<?> createPlanPremium(@RequestBody @Valid CreatePaymentDTO createPaymentDTO, BindingResult validation,
                                               @RequestHeader("X-Idempotency-Key") String idempotencyKey,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        if(validation.hasErrors()){
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            String PREMIUM_PLAN = "premium";
            PaymentResponseDTO result = paymentService.createPayment(createPaymentDTO, userDetails, idempotencyKey, PREMIUM_PLAN);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("payments/{id}")
    public ResponseEntity<?> listPayment(@PathVariable("id") UUID id,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try{
            PaymentResponseDTO result = paymentService.listPayment(id, userDetails);

            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("payments")
    public ResponseEntity<?> listPayments(@AuthenticationPrincipal UserDetails userDetails) {
        try{
            List<PaymentResponseDTO> result = paymentService.listPayments(userDetails);

            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("payments/refund")
    public ResponseEntity<?> refundPayment(@RequestBody @Valid String paymentDetails, BindingResult validation) {
        if(validation.hasErrors()){
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        // Logic to process the refund
        // For now, returning a placeholder response
        return ResponseEntity.ok("Payment refunded successfully");
    }
}
