package com.bookhaus.shelf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateShelfRequest(@NotBlank @Size(max = 120) String name) {
}
