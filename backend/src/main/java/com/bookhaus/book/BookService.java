package com.bookhaus.book;

import com.bookhaus.book.dto.BookDetailsResponse;
import com.bookhaus.book.dto.BookSearchResult;
import com.bookhaus.book.external.BookProvider;
import com.bookhaus.book.external.BookProviderException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookService {
    private final BookProvider bookProvider;
    private final BookRepository bookRepository;

    public BookService(BookProvider bookProvider, BookRepository bookRepository) {
        this.bookProvider = bookProvider;
        this.bookRepository = bookRepository;
    }

    public List<BookSearchResult> search(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query must not be blank");
        }
        try {
            return bookProvider.search(normalizedQuery);
        } catch (BookProviderException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, exception.getMessage(), exception);
        }
    }

    @Transactional(readOnly = true)
    public BookDetailsResponse getById(Long id) {
        return bookRepository.findById(id)
                .map(BookDetailsResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }
}
