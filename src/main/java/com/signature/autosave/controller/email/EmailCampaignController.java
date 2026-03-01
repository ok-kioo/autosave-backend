package com.signature.autosave.controller.email;

import com.mercadopago.net.HttpStatus;
import com.signature.autosave.modules.email.campaign.dto.CreateEmailCampaignDTO;
import com.signature.autosave.modules.email.campaign.dto.EmailCampaignResponseDTO;
import com.signature.autosave.modules.email.campaign.service.EmailCampaignService;
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
public class EmailCampaignController {
    private final EmailCampaignService emailCampaignService;

    @PostMapping("/create")
    public ResponseEntity<?> createEmailCampaign(@RequestBody CreateEmailCampaignDTO createEmailCampaignDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            EmailCampaignResponseDTO result = emailCampaignService.createEmailCampaign(createEmailCampaignDTO, userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listEmailCampaigns(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<EmailCampaignResponseDTO> result = emailCampaignService.listEmailCampaigns(userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> listEmailCampaignsAvailable(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<EmailCampaignResponseDTO> result = emailCampaignService.listEmailCampaignsAvailable(userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<?> listEmailCampaigns(@PathVariable UUID id) {
        try {
            EmailCampaignResponseDTO result = emailCampaignService.listEmailCampaign(id);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEmailCampaign(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            emailCampaignService.deleteEmailCampaign(id, userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(Map.of("message", "Email campaign. deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }


}

