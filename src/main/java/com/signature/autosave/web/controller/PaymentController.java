package com.signature.autosave.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class PaymentController {

    @PostMapping("/payments")
    public ResponseEntity<?> createPayment(@RequestBody @Valid String paymentDetails, BindingResult validation) {
        if(validation.hasErrors()){
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }



        return ResponseEntity.ok("Payment generated successfully");
    }

    @GetMapping("/payments")
    public ResponseEntity<?> listPayments() {
        // Logic to retrieve payment details by ID
        // For now, returning a placeholder response
        return ResponseEntity.ok(Map.of("status", "Payment details retrieved successfully"));
    }

    @PostMapping("/payments/refund")
    public ResponseEntity<?> refundPayment(@RequestBody @Valid String paymentDetails, BindingResult validation) {
        if(validation.hasErrors()){
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        // Logic to process the refund
        // For now, returning a placeholder response
        return ResponseEntity.ok("Payment refunded successfully");
    }
}
