package com.signature.autosave.infra.components.jwt;

import org.springframework.security.core.userdetails.UserDetails;

public interface IAuthComponent {
    String generateToken(UserDetails userDetails);

    String getUsernameFromToken(String token);

    boolean validateToken(String token);
}
