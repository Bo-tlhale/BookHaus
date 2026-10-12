package com.bookhaus.auth;

import com.bookhaus.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
        "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)
    );
    private static final String OTHER_SECRET = Base64.getEncoder().encodeToString(
        "abcdef0123456789abcdef0123456789".getBytes(StandardCharsets.UTF_8)
    );

    @Test
    void generatesAndValidatesTokenForUser() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        User user = user();
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password("unused")
            .authorities(java.util.Collections.emptyList())
            .build();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("jane@example.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() {
        JwtService issuer = new JwtService(SECRET, 60_000);
        JwtService validator = new JwtService(OTHER_SECRET, 60_000);
        User user = user();
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password("unused")
            .authorities(java.util.Collections.emptyList())
            .build();

        assertThat(validator.isTokenValid(issuer.generateToken(user), userDetails)).isFalse();
    }

    private User user() {
        User user = new User();
        user.setId(42L);
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPasswordHash("unused");
        return user;
    }
}
