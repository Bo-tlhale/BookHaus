package com.bookhaus.auth.dto;

import com.bookhaus.user.dto.UserResponse;

public record AuthResponse(
    String token,
    UserResponse user
) {
}
