package com.signature.autosave.controller.payment;

import com.mercadopago.net.HttpStatus;
import com.signature.autosave.modules.payment.payload.dto.PayloadResponseDTO;
import com.signature.autosave.modules.payment.payload.service.PayloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payload")
public class PayloadController {
    private final PayloadService payloadService;

    @GetMapping
    public ResponseEntity<?> listPayloads(@PageableDefault(size = 10, sort = "type") Pageable pageable) {
        try {
            Page<PayloadResponseDTO> result = payloadService.listPayloads(pageable);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<?> listPayloads(@PathVariable UUID id) {
        try {
            PayloadResponseDTO result = payloadService.listPayload(id);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }


}

