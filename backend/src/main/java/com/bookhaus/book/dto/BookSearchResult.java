package com.bookhaus.book.dto;

import java.util.List;

public record BookSearchResult(
        String externalId,
        String title,
        List<String> authors,
        String description,
        String isbn,
        String coverUrl,
        List<String> genres,
        String publisher,
        Integer publicationYear,
        Integer pageCount
) {
}
