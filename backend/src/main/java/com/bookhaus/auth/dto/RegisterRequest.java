package com.bookhaus.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record RegisterRequest(
    @NotBlank
    @Size(max = 120)
    String name,

    @NotBlank
    @Email
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        message = "Email must contain a valid domain"
    )
    @Size(max = 255)
    String email,

    @NotBlank
    @Size(min = 8, max = 72)
    String password
) {
    public RegisterRequest {
        if (name != null) {
            name = name.trim().replaceAll("\\s+", " ");
        }
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }
}
