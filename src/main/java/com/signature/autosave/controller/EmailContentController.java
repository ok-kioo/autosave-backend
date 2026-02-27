package com.signature.autosave.controller;

import com.mercadopago.net.HttpStatus;
import com.signature.autosave.modules.email.content.dto.CreateEmailContentDTO;
import com.signature.autosave.modules.email.content.dto.EmailContentResponseDTO;
import com.signature.autosave.modules.email.content.dto.UpdateEmailContentDTO;
import com.signature.autosave.modules.email.content.service.EmailContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email/content")
public class EmailContentController {
    private final EmailContentService emailContentService;

    @PostMapping("/create")
    public ResponseEntity<?> createEmailContent(@RequestBody CreateEmailContentDTO createEmailContentDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            EmailContentResponseDTO result = emailContentService.createEmailContent(createEmailContentDTO, userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updateEmailContent(@PathVariable UUID id, @RequestBody UpdateEmailContentDTO updateEmailContentDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            EmailContentResponseDTO result = emailContentService.updateEmailContent(id, updateEmailContentDTO, userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listEmailContents(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<EmailContentResponseDTO> result = emailContentService.listEmailContents(userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<?> listEmailContents(@PathVariable UUID id) {
        try {
            EmailContentResponseDTO result = emailContentService.listEmailContent(id);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEmailContent(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            emailContentService.deleteEmailContent(id, userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(Map.of("message", "Email content deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }


}

