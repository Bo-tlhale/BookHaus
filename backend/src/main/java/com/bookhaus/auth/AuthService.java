package com.bookhaus.auth;

import com.bookhaus.auth.dto.AuthResponse;
import com.bookhaus.auth.dto.LoginRequest;
import com.bookhaus.auth.dto.RegisterRequest;
import com.bookhaus.user.User;
import com.bookhaus.user.UserRepository;
import com.bookhaus.user.dto.UserResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        User user = new User();
        user.setName(normalizeName(request.name()));
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return createAuthResponse(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An account with this email already exists",
                exception
            );
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
            );
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password", exception);
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }
}
