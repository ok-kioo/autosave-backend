package com.signature.autosave.controller.node;

import com.mercadopago.net.HttpStatus;
import com.signature.autosave.modules.email.campaign.dto.node.CommentThreadProjection;
import com.signature.autosave.modules.email.campaign.dto.node.RegisterCampaignNodeViewDTO;
import com.signature.autosave.modules.email.campaign.dto.node.ToggleLikeCampaignNodeDTO;
import com.signature.autosave.modules.email.campaign.service.CampaignNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email/campaign/node")
public class CampaignNodeController {
    private final CampaignNodeService campaignNodeService;

    @PostMapping("/views/register")
    public ResponseEntity<?> registerView(@RequestBody RegisterCampaignNodeViewDTO registerCampaignNodeViewDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            campaignNodeService.registerView(registerCampaignNodeViewDTO, userDetails);
            return ResponseEntity.ok(Map.of("message", "View registered"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/views/{id}")
    public ResponseEntity<?> countViews(@PathVariable UUID id) {
        try {
            Long result = campaignNodeService.countViews(id);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/likes/toggle")
    public ResponseEntity<?> toggleLike(@RequestBody ToggleLikeCampaignNodeDTO toggleLikeCampaignNodeDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Boolean result = campaignNodeService.toggleLike(toggleLikeCampaignNodeDTO, userDetails);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/likes/{id}")
    public ResponseEntity<?> countLikes(@PathVariable UUID id) {
        try {
            Long result = campaignNodeService.countLikes(id);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<?> listComments(@PathVariable UUID id, @PageableDefault(size = 10) Pageable pageable) {
        try {
            List<CommentThreadProjection> result = campaignNodeService.listComments(id, pageable);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }



}

