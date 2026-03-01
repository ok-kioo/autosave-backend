package com.signature.autosave.controller.email;

import com.mercadopago.net.HttpStatus;
import com.signature.autosave.modules.email.campaign.dto.CreateEmailCampaignReviewDTO;
import com.signature.autosave.modules.email.campaign.dto.EmailCampaignReviewResponseDTO;
import com.signature.autosave.modules.email.campaign.dto.UpdateEmailCampaignReviewDTO;
import com.signature.autosave.modules.email.campaign.service.EmailCampaignReviewService;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/email/campaign")
public class EmailCampaignReviewController {
    private final EmailCampaignReviewService emailCampaignReviewService;

    @PostMapping("/review/create")
    public ResponseEntity<?> createEmailCampaignReview(@RequestBody CreateEmailCampaignReviewDTO createEmailCampaignReviewDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            EmailCampaignReviewResponseDTO result = emailCampaignReviewService.createEmailCampaignReview(createEmailCampaignReviewDTO, userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/review/update/{id}")
    public ResponseEntity<?> updateEmailCampaignReview(@PathVariable UUID id, @RequestBody UpdateEmailCampaignReviewDTO updateEmailCampaignReviewDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            EmailCampaignReviewResponseDTO result = emailCampaignReviewService.updateEmailCampaignReview(id, updateEmailCampaignReviewDTO, userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/review")
    public ResponseEntity<?> listEmailCampaignReviews(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<EmailCampaignReviewResponseDTO> result = emailCampaignReviewService.listEmailCampaignReviews(userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/reviews")
    public ResponseEntity<?> listEmailCampaignReviewsByEmailCampaign(@RequestBody @NotNull UUID emailCampaignId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<EmailCampaignReviewResponseDTO> result = emailCampaignReviewService.listEmailCampaignReviewsByEmailCampaign(emailCampaignId, userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/review/{id}")
    public ResponseEntity<?> listEmailCampaignReview(@PathVariable UUID id) {
        try {
            EmailCampaignReviewResponseDTO result = emailCampaignReviewService.listEmailCampaignReview(id);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/review/delete/{id}")
    public ResponseEntity<?> deleteEmailCampaignReview(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            emailCampaignReviewService.deleteEmailCampaignReview(id, userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(Map.of("message", "Email campaign. deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }


}

