package com.signature.autosave.controller.subscription;

import com.signature.autosave.modules.subscription.dto.CreateSubscriptionPlanDTO;
import com.signature.autosave.modules.subscription.dto.SubscriptionPlanResponseDTO;
import com.signature.autosave.modules.subscription.dto.UpdateSubscriptionPlanDTO;
import com.signature.autosave.modules.subscription.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionPlanController {
    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping("/create")
    public ResponseEntity<?> register(@RequestBody @Valid CreateSubscriptionPlanDTO newSubscriptionPlan, BindingResult validation) {
        if (validation.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            SubscriptionPlanResponseDTO result = subscriptionPlanService.createSubscriptionPlan(newSubscriptionPlan);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> listSubscription(@PathVariable("id") UUID id) {

        try{
            SubscriptionPlanResponseDTO result = subscriptionPlanService.listSubscription(id);
            return ResponseEntity.status(HttpStatus.FOUND).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> listSubscriptions(@PageableDefault(size = 10, sort = "name") Pageable pageable) {
        try{
            Page<SubscriptionPlanResponseDTO> result = subscriptionPlanService.listSubscriptions(pageable);

            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(com.mercadopago.net.HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("update/{id}")
    public ResponseEntity<?> update(@RequestBody @Valid UpdateSubscriptionPlanDTO updatedSubscriptionPlan, BindingResult validation,
                                    @PathVariable("id") UUID id) {
        if (validation.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            SubscriptionPlanResponseDTO result = subscriptionPlanService.updateSubscriptionPlan(updatedSubscriptionPlan, id);
            return ResponseEntity.status(HttpStatus.OK).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        try{
            subscriptionPlanService.deleteSubscriptionPlan(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Plano deletado com sucesso");

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

}
