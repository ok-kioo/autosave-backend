package com.signature.autosave.controller;

import com.signature.autosave.modules.user.dto.*;
import com.signature.autosave.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("auth/create")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO newUser, BindingResult validation) {
        if (validation.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            UserResponseDTO result = userService.createUser(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("users/{id}")
    public ResponseEntity<?> list(@PathVariable("id") UUID id) {

        try{
            UserResponseDTO result = userService.list(id);
            return ResponseEntity.status(HttpStatus.FOUND).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("users/update/{id}")
    public ResponseEntity<?> update(@RequestBody @Valid UpdateUserDTO updatedUser, BindingResult validation,
                                    @PathVariable("id") UUID id,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (validation.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            UserResponseDTO result = userService.updateUser(updatedUser, id, userDetails);
            return ResponseEntity.status(HttpStatus.OK).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("users/update/role/{id}")
    public ResponseEntity<?> updateRole(@RequestBody @Valid UpdateRoleUserDTO updatedRoleUser, BindingResult validation,
                                    @PathVariable("id") UUID id) {
        if (validation.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            UserResponseDTO result = userService.updateRoleUser(updatedRoleUser, id);
            return ResponseEntity.status(HttpStatus.OK).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("users/delete/{id}")
    public ResponseEntity<?> delete(@RequestBody @Valid DeleteUserDTO deleteUser, BindingResult validation,
                                    @PathVariable("id") UUID id,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (validation.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            userService.deleteUser(deleteUser, id, userDetails);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Usuário deletado com sucesso");

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

}
