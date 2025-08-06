package com.signature.autosave.web.controller;

import com.mercadopago.net.HttpStatus;
import com.signature.autosave.modules.payment.dto.CreatePaymentDTO;
import com.signature.autosave.modules.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("")
    public ResponseEntity<?> createPayment(@RequestBody @Valid CreatePaymentDTO createPaymentDTO, BindingResult validation,
                                           @RequestHeader("X-Idempotency-Key") String idempotencyKey,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        if(validation.hasErrors()){
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            paymentService.createPayment(createPaymentDTO, userDetails,idempotencyKey);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }

        return ResponseEntity.ok(Map.of("status", "Payment created successfully"));
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<?> createSubscription(@RequestBody @Valid CreatePaymentDTO createPaymentDTO, BindingResult validation,
                                           @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        if(validation.hasErrors()){
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            /*paymentService.createPayment(createPaymentDTO, idempotencyKey);*/

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }

        return ResponseEntity.ok(Map.of("status", "Subscription created successfully"));
    }

    @GetMapping("")
    public ResponseEntity<?> listPayments() {
        // Logic to retrieve payment details by ID
        // For now, returning a placeholder response
        return ResponseEntity.ok(Map.of("status", "Payment details retrieved successfully"));
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(@RequestBody @Valid String paymentDetails, BindingResult validation) {
        if(validation.hasErrors()){
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        // Logic to process the refund
        // For now, returning a placeholder response
        return ResponseEntity.ok("Payment refunded successfully");
    }
}
