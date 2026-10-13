package com.bookhaus.book.dto;

import com.bookhaus.book.Book;
import java.util.List;

public record BookDetailsResponse(
        Long id,
        String title,
        List<String> authors,
        String description,
        String isbn10,
        String isbn13,
        String coverUrl,
        List<String> genres,
        String publisher,
        Integer publicationYear,
        Integer pageCount,
        String externalProvider,
        String externalBookId
) {
    public static BookDetailsResponse from(Book book) {
        return new BookDetailsResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthors().stream().map(author -> author.getName()).sorted(String.CASE_INSENSITIVE_ORDER).toList(),
                book.getDescription(),
                book.getIsbn10(),
                book.getIsbn13(),
                book.getCoverUrl(),
                book.getGenres().stream().map(genre -> genre.getName()).sorted(String.CASE_INSENSITIVE_ORDER).toList(),
                book.getPublisher(),
                book.getPublicationYear(),
                book.getPageCount(),
                book.getExternalProvider(),
                book.getExternalBookId()
        );
    }
}
