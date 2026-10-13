package com.bookhaus.library.dto;

import com.bookhaus.book.dto.BookDetailsResponse;
import com.bookhaus.library.LibraryBook;
import com.bookhaus.library.ReadingStatus;
import java.time.Instant;

public record LibraryBookResponse(
        Long id,
        ReadingStatus status,
        Integer currentPage,
        Instant addedAt,
        BookDetailsResponse book
) {
    public static LibraryBookResponse from(LibraryBook libraryBook) {
        return new LibraryBookResponse(
                libraryBook.getId(),
                libraryBook.getStatus(),
                libraryBook.getCurrentPage(),
                libraryBook.getAddedAt(),
                BookDetailsResponse.from(libraryBook.getBook())
        );
    }
}
