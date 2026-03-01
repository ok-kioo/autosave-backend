package com.signature.autosave.controller.contract;

import com.mercadopago.net.HttpStatus;
import com.signature.autosave.modules.contract.dto.CreatePlanContractDTO;
import com.signature.autosave.modules.contract.dto.PlanContractResponseDTO;
import com.signature.autosave.modules.contract.service.PlanContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contract")
public class PlanContractController {
    private final PlanContractService planContractService;

    @PostMapping("/create")
    public ResponseEntity<?> contract(@RequestBody @Valid CreatePlanContractDTO createPlanContractDTO, BindingResult validation,
                                             @RequestHeader("X-Idempotency-Key") String idempotencyKey,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        if(validation.hasErrors()){
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            PlanContractResponseDTO result = planContractService.createPlanContract(createPlanContractDTO, userDetails, idempotencyKey);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> listPlanContract(@PathVariable("id") UUID id,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try{
            PlanContractResponseDTO result = planContractService.listPlanContract(id, userDetails);

            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> listPlanContracts(@AuthenticationPrincipal UserDetails userDetails) {
        try{
            List<PlanContractResponseDTO> result = planContractService.listPlanContracts(userDetails);

            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelPlanContract(@PathVariable("id") UUID id,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try{
            PlanContractResponseDTO result = planContractService.cancelPlanContract(id, userDetails);

            return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refund/{id}")
    public ResponseEntity<?> refundPlanContract(@PathVariable("id") UUID id,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        try{
            PlanContractResponseDTO result = planContractService.refundPlanContract(id, userDetails);

            return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
