package com.bookhaus.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record LoginRequest(
    @NotBlank
    @Email
    @Size(max = 255)
    String email,

    @NotBlank
    @Size(max = 72)
    String password
) {
    public LoginRequest {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }
}
