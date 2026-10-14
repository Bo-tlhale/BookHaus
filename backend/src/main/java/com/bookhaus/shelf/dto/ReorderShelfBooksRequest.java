package com.bookhaus.shelf.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record ReorderShelfBooksRequest(
        @NotEmpty List<@NotNull @Positive Long> libraryBookIds
) {
}
