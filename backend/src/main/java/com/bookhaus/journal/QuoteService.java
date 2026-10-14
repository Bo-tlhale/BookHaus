package com.bookhaus.journal;

import com.bookhaus.journal.dto.CreateQuoteRequest;
import com.bookhaus.journal.dto.QuoteResponse;
import com.bookhaus.journal.dto.UpdateQuoteRequest;
import com.bookhaus.library.LibraryBook;
import com.bookhaus.library.LibraryBookRepository;
import com.bookhaus.user.UserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuoteService {
    private final UserService userService;
    private final LibraryBookRepository libraryBookRepository;
    private final QuoteRepository quoteRepository;

    public QuoteService(
            UserService userService,
            LibraryBookRepository libraryBookRepository,
            QuoteRepository quoteRepository
    ) {
        this.userService = userService;
        this.libraryBookRepository = libraryBookRepository;
        this.quoteRepository = quoteRepository;
    }

    @Transactional
    public QuoteResponse createQuote(Long libraryBookId, CreateQuoteRequest request) {
        LibraryBook libraryBook = findOwnedLibraryBook(libraryBookId);
        Quote quote = new Quote();
        quote.setLibraryBook(libraryBook);
        quote.setQuoteText(request.quoteText().trim());
        quote.setPageNumber(request.pageNumber());
        quote.setNote(request.note());
        return QuoteResponse.from(quoteRepository.saveAndFlush(quote));
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> getQuotes(Long libraryBookId) {
        LibraryBook libraryBook = findOwnedLibraryBook(libraryBookId);
        return quoteRepository.findAllByLibraryBookOrderByCreatedAtAsc(libraryBook).stream()
                .map(QuoteResponse::from)
                .toList();
    }

    @Transactional
    public QuoteResponse updateQuote(Long libraryBookId, Long quoteId, UpdateQuoteRequest request) {
        LibraryBook libraryBook = findOwnedLibraryBook(libraryBookId);
        Quote quote = findQuote(quoteId, libraryBook);
        if (request.isQuoteTextSupplied()) {
            quote.setQuoteText(request.quoteText().trim());
        }
        if (request.isPageNumberSupplied()) {
            quote.setPageNumber(request.pageNumber());
        }
        if (request.isNoteSupplied()) {
            quote.setNote(request.note());
        }
        return QuoteResponse.from(quoteRepository.saveAndFlush(quote));
    }

    @Transactional
    public void deleteQuote(Long libraryBookId, Long quoteId) {
        LibraryBook libraryBook = findOwnedLibraryBook(libraryBookId);
        quoteRepository.delete(findQuote(quoteId, libraryBook));
    }

    private LibraryBook findOwnedLibraryBook(Long libraryBookId) {
        return libraryBookRepository.findByIdAndUser(libraryBookId, userService.getCurrentUserEntity())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Library book not found"));
    }

    private Quote findQuote(Long quoteId, LibraryBook libraryBook) {
        return quoteRepository.findByIdAndLibraryBook(quoteId, libraryBook)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quote not found"));
    }
}
