package com.bookhaus.journal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateQuoteRequest(
        @NotBlank String quoteText,
        @Positive Integer pageNumber,
        String note
) {
}
