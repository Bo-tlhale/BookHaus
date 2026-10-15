package com.bookhaus.goal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateReadingGoalRequest(
        @NotNull @Min(1900) @Max(9999) Integer year,
        @NotNull @Positive Integer targetBooks
) {
}
