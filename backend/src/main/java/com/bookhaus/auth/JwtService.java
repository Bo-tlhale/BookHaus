package com.bookhaus.auth;

import com.bookhaus.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
        @Value("${bookhaus.jwt.secret}") String encodedSecret,
        @Value("${bookhaus.jwt.expiration}") long expirationMillis
    ) {
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("JWT expiration must be greater than zero");
        }

        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(User user) {
        Instant issuedAt = Instant.now();
        return Jwts.builder()
            .subject(user.getEmail())
            .claim("userId", user.getId())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(issuedAt.plusMillis(expirationMillis)))
            .signWith(signingKey)
            .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = extractClaims(token);
            return claims.getSubject().equals(userDetails.getUsername())
                && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
