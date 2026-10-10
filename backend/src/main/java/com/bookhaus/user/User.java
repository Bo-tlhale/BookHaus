package com.bookhaus.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.Locale;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank
    @Email
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        message = "Email must contain a valid domain"
    )
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        normalize();
        createdAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        normalize();
    }

    private void normalize() {
        if (name != null) {
            name = name.trim().replaceAll("\\s+", " ");
        }

        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }
}
