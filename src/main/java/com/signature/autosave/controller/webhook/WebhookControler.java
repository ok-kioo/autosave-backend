package com.signature.autosave.controller.webhook;

import com.mercadopago.net.HttpStatus;
import com.signature.autosave.modules.payment.payload.service.PayloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks/mercadopago")
public class WebhookControler {
    private final PayloadService payloadService;

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(@RequestBody Map<String, Object> payload) {

        try{
            payloadService.processPayload(payload);

        }catch (Exception e) {
            return (ResponseEntity<Void>) ResponseEntity.status(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok().build();
    }

}

