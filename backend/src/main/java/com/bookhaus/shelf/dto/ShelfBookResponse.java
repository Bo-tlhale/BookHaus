package com.bookhaus.shelf.dto;

import com.bookhaus.library.ReadingStatus;
import com.bookhaus.shelf.ShelfBook;
import java.time.Instant;
import java.util.List;

public record ShelfBookResponse(
        Long libraryBookId,
        Long bookId,
        String title,
        List<String> authors,
        String coverUrl,
        ReadingStatus readingStatus,
        Integer position,
        Instant addedAt
) {
    public static ShelfBookResponse from(ShelfBook shelfBook) {
        var libraryBook = shelfBook.getLibraryBook();
        var book = libraryBook.getBook();
        return new ShelfBookResponse(
                libraryBook.getId(),
                book.getId(),
                book.getTitle(),
                book.getAuthors().stream().map(author -> author.getName()).sorted(String.CASE_INSENSITIVE_ORDER).toList(),
                book.getCoverUrl(),
                libraryBook.getStatus(),
                shelfBook.getPosition(),
                shelfBook.getAddedAt()
        );
    }
}
