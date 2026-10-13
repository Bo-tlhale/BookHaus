package com.bookhaus.book.dto;

import com.bookhaus.library.ReadingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AddBookRequest(
        @NotBlank @Size(max = 255) String externalId,
        @NotBlank @Size(max = 500) String title,
        @Size(max = 20) List<@NotBlank @Size(max = 255) String> authors,
        String description,
        @Size(max = 32) String isbn,
        String coverUrl,
        @Size(max = 10) List<@NotBlank @Size(max = 120) String> genres,
        @Size(max = 255) String publisher,
        @Positive Integer publicationYear,
        @Positive Integer pageCount,
        ReadingStatus status
) {
}
