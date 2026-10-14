package com.bookhaus.shelf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameShelfRequest(@NotBlank @Size(max = 120) String name) {
}
