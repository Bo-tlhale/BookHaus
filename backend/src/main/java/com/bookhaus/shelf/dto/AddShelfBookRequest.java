package com.bookhaus.shelf.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddShelfBookRequest(@NotNull @Positive Long libraryBookId) {
}
