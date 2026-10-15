package com.bookhaus.library.dto;

import com.bookhaus.library.LibraryBook;
import com.bookhaus.library.ReadingStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record LibraryBookResponse(
        Long libraryBookId,
        Long bookId,
        String title,
        List<String> authors,
        String coverUrl,
        List<String> genres,
        ReadingStatus readingStatus,
        Integer currentPage,
        Integer pageCount,
        BigDecimal progressPercentage,
        BigDecimal personalRating,
        LocalDate startedDate,
        LocalDate finishedDate,
        Instant addedAt
) {
    public static LibraryBookResponse from(LibraryBook libraryBook) {
        var book = libraryBook.getBook();
        return new LibraryBookResponse(
                libraryBook.getId(),
                book.getId(),
                book.getTitle(),
                book.getAuthors().stream().map(author -> author.getName()).sorted(String.CASE_INSENSITIVE_ORDER).toList(),
                book.getCoverUrl(),
                book.getGenres().stream().map(genre -> genre.getName()).sorted(String.CASE_INSENSITIVE_ORDER).toList(),
                libraryBook.getStatus(),
                libraryBook.getCurrentPage(),
                book.getPageCount(),
                progressPercentage(libraryBook.getStatus(), libraryBook.getCurrentPage(), book.getPageCount()),
                libraryBook.getPersonalRating(),
                libraryBook.getStartedDate(),
                libraryBook.getFinishedDate(),
                libraryBook.getAddedAt()
        );
    }

    private static BigDecimal progressPercentage(ReadingStatus status, Integer currentPage, Integer pageCount) {
        if (currentPage == null || pageCount == null || pageCount <= 0) {
            return null;
        }
        if (status == ReadingStatus.COMPLETED) {
            return new BigDecimal("100.0");
        }
        int boundedPage = Math.min(Math.max(currentPage, 0), pageCount);
        return BigDecimal.valueOf(boundedPage)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(pageCount), 1, RoundingMode.HALF_UP);
    }
}
