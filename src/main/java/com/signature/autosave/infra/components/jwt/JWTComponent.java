package com.signature.autosave.infra.components.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JWTComponent implements IAuthComponent{
    private final SecretKey JWT_SECRET = Keys.hmacShaKeyFor(System.getenv("JWT_TOKEN_SECRET").getBytes());

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        long EXPIRATION = 86400000L;
        Date expiry = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(JWT_SECRET)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        JwtParser parser = Jwts.parser()
                .verifyWith(JWT_SECRET)
                .build();

        return parser
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(JWT_SECRET)
                    .build();

            parser.parseSignedClaims(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

