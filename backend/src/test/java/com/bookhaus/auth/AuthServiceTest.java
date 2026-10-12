package com.bookhaus.auth;

import com.bookhaus.auth.dto.AuthResponse;
import com.bookhaus.auth.dto.LoginRequest;
import com.bookhaus.auth.dto.RegisterRequest;
import com.bookhaus.user.User;
import com.bookhaus.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void registersUserAndReturnsSafeResponse() {
        RegisterRequest request = new RegisterRequest(
            "  Jane   Doe  ",
            "  JANE.DOE@EXAMPLE.COM  ",
            "correct-horse-battery-staple"
        );
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("signed-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("signed-token");
        assertThat(response.user().id()).isEqualTo(1L);
        assertThat(response.user().name()).isEqualTo("Jane Doe");
        assertThat(response.user().email()).isEqualTo("jane.doe@example.com");
    }

    @Test
    void rejectsDuplicateRegistration() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "JANE@EXAMPLE.COM", "strong-password");
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void storesPasswordAsBcryptHash() {
        String plainPassword = "strong-password";
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", plainPassword);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("signed-token");

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        String storedPassword = captor.getValue().getPasswordHash();
        assertThat(storedPassword).isNotEqualTo(plainPassword);
        assertThat(storedPassword).startsWith("$2");
        assertThat(passwordEncoder.matches(plainPassword, storedPassword)).isTrue();
    }

    @Test
    void logsInWithNormalizedEmailAndReturnsToken() {
        User user = user(7L, "Jane Doe", "jane@example.com", "$2a$10$storedHash");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("signed-token");

        AuthResponse response = authService.login(
            new LoginRequest("  JANE@EXAMPLE.COM ", "strong-password")
        );

        assertThat(response.token()).isEqualTo("signed-token");
        assertThat(response.user().id()).isEqualTo(7L);
        verify(authenticationManager).authenticate(argThat(authentication ->
            authentication.getName().equals("jane@example.com")
                && authentication.getCredentials().equals("strong-password")));
    }

    @Test
    void rejectsIncorrectPasswordAsUnauthorized() {
        doThrow(new BadCredentialsException("Bad credentials"))
            .when(authenticationManager)
            .authenticate(any());

        assertThatThrownBy(() -> authService.login(
            new LoginRequest("jane@example.com", "incorrect-password")
        )).isInstanceOfSatisfying(ResponseStatusException.class, exception ->
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));

        verify(userRepository, never()).findByEmail(any());
    }

    private User user(Long id, String name, String email, String passwordHash) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        return user;
    }
}
