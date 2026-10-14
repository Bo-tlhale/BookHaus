package com.bookhaus.library.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

public record UpdateRatingRequest(
        @DecimalMin("0.0") @DecimalMax("5.0") @Digits(integer = 1, fraction = 1) BigDecimal personalRating
) {
}
