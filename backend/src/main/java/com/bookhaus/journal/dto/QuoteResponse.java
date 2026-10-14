package com.bookhaus.journal.dto;

import com.bookhaus.journal.Quote;
import java.time.Instant;

public record QuoteResponse(
        Long id,
        Long libraryBookId,
        String quoteText,
        Integer pageNumber,
        String note,
        Instant createdAt
) {
    public static QuoteResponse from(Quote quote) {
        return new QuoteResponse(
                quote.getId(),
                quote.getLibraryBook().getId(),
                quote.getQuoteText(),
                quote.getPageNumber(),
                quote.getNote(),
                quote.getCreatedAt()
        );
    }
}
