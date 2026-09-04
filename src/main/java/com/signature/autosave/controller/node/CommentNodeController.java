package com.signature.autosave.controller.node;

import com.mercadopago.net.HttpStatus;
import com.signature.autosave.modules.email.campaign.dto.node.CommentThreadProjection;
import com.signature.autosave.modules.email.campaign.dto.node.RegisterCampaignCommentDTO;
import com.signature.autosave.modules.email.campaign.dto.node.RegisterReplyCommentDTO;
import com.signature.autosave.modules.email.campaign.service.CampaignNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email/campaign/node/comments")
public class CommentNodeController {
    private final CampaignNodeService campaignNodeService;

    @PostMapping("/register")
    public ResponseEntity<?> registerCampaignComment(@RequestBody RegisterCampaignCommentDTO registerCampaignCommentDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            CommentThreadProjection result = campaignNodeService.registerCampaignComment(registerCampaignCommentDTO, userDetails);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reply/register")
    public ResponseEntity<?> registerReplyComment(@RequestBody RegisterReplyCommentDTO registerReplyCommentDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            CommentThreadProjection result = campaignNodeService.registerReplyComment(registerReplyCommentDTO, userDetails);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteComment(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            campaignNodeService.deleteComment(id, userDetails);
            return ResponseEntity.ok(Map.of("message", "Comment deleted"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

}

