package com.signature.autosave.controller;

import com.signature.autosave.dto.user.*;
import com.signature.autosave.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/auth/register")
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

    @GetMapping("/user/list")
    public ResponseEntity<?> list(@RequestBody @Valid ListUserDTO users, BindingResult validation) {
        if (validation.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            List<UserResponseDTO> result = userService.listUser(users);
            return ResponseEntity.status(HttpStatus.FOUND).body(result);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/user/{id}/update")
    public ResponseEntity<?> update(@RequestBody @Valid UpdateUserDTO updatedUser, BindingResult validation,
                                    @PathVariable UUID id,
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

    @DeleteMapping("/user/{id}/delete")
    public ResponseEntity<?> delete(@RequestBody @Valid DeleteUserDTO deleteUser, BindingResult validation,
                                    @PathVariable UUID id,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (validation.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", validation.getFieldErrors()));
        }

        try{
            userService.deleteUser(deleteUser, id, userDetails);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User deleted successfully");

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

}
