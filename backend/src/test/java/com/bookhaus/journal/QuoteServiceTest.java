package com.bookhaus.journal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhaus.book.Book;
import com.bookhaus.journal.dto.CreateQuoteRequest;
import com.bookhaus.journal.dto.UpdateQuoteRequest;
import com.bookhaus.library.LibraryBook;
import com.bookhaus.library.LibraryBookRepository;
import com.bookhaus.library.ReadingStatus;
import com.bookhaus.user.User;
import com.bookhaus.user.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {
    @Mock UserService userService;
    @Mock LibraryBookRepository libraryBookRepository;
    @Mock QuoteRepository quoteRepository;
    private QuoteService service;
    private User currentUser;
    private LibraryBook libraryBook;

    @BeforeEach
    void setUp() {
        service = new QuoteService(userService, libraryBookRepository, quoteRepository);
        currentUser = new User();
        currentUser.setId(7L);
        currentUser.setEmail("reader@example.com");
        libraryBook = libraryBook(12L);
        when(userService.getCurrentUserEntity()).thenReturn(currentUser);
    }

    @Test
    void createsQuoteWithOptionalPageAndNoteAndTrimsOnlyQuoteText() {
        when(libraryBookRepository.findByIdAndUser(12L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(quoteRepository.saveAndFlush(any(Quote.class))).thenAnswer(invocation -> {
            Quote quote = invocation.getArgument(0);
            quote.setId(30L);
            quote.setCreatedAt(Instant.parse("2026-10-14T10:00:00Z"));
            return quote;
        });

        var response = service.createQuote(12L,
                new CreateQuoteRequest("  Fear is the mind-killer.  ", 25, "  Keep this spacing.  "));

        assertThat(response.quoteText()).isEqualTo("Fear is the mind-killer.");
        assertThat(response.pageNumber()).isEqualTo(25);
        assertThat(response.note()).isEqualTo("  Keep this spacing.  ");
    }

    @Test
    void createsQuoteWithoutOptionalFields() {
        when(libraryBookRepository.findByIdAndUser(12L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(quoteRepository.saveAndFlush(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createQuote(12L, new CreateQuoteRequest("A quote", null, null));

        assertThat(response.pageNumber()).isNull();
        assertThat(response.note()).isNull();
    }

    @Test
    void listsOnlyOwnedBooksQuotesInAscendingCreationOrder() {
        Quote first = quote(30L, libraryBook, "First", "2026-10-14T09:00:00Z");
        Quote second = quote(31L, libraryBook, "Second", "2026-10-14T10:00:00Z");
        when(libraryBookRepository.findByIdAndUser(12L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(quoteRepository.findAllByLibraryBookOrderByCreatedAtAsc(libraryBook))
                .thenReturn(List.of(first, second));

        assertThat(service.getQuotes(12L)).extracting(response -> response.quoteText())
                .containsExactly("First", "Second");
    }

    @Test
    void updatesOnlySuppliedQuoteFields() {
        Quote quote = quote(30L, libraryBook, "Original", "2026-10-14T09:00:00Z");
        quote.setPageNumber(12);
        quote.setNote("Original note");
        UpdateQuoteRequest request = new UpdateQuoteRequest();
        request.setQuoteText("  Updated quote  ");
        when(libraryBookRepository.findByIdAndUser(12L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(quoteRepository.findByIdAndLibraryBook(30L, libraryBook)).thenReturn(Optional.of(quote));
        when(quoteRepository.saveAndFlush(quote)).thenReturn(quote);

        var response = service.updateQuote(12L, 30L, request);

        assertThat(response.quoteText()).isEqualTo("Updated quote");
        assertThat(response.pageNumber()).isEqualTo(12);
        assertThat(response.note()).isEqualTo("Original note");
    }

    @Test
    void explicitNullClearsOptionalQuoteFields() {
        Quote quote = quote(30L, libraryBook, "Original", "2026-10-14T09:00:00Z");
        quote.setPageNumber(12);
        quote.setNote("Original note");
        UpdateQuoteRequest request = new UpdateQuoteRequest();
        request.setPageNumber(null);
        request.setNote(null);
        when(libraryBookRepository.findByIdAndUser(12L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(quoteRepository.findByIdAndLibraryBook(30L, libraryBook)).thenReturn(Optional.of(quote));
        when(quoteRepository.saveAndFlush(quote)).thenReturn(quote);

        var response = service.updateQuote(12L, 30L, request);

        assertThat(response.pageNumber()).isNull();
        assertThat(response.note()).isNull();
    }

    @Test
    void cannotUpdateQuoteThroughAnotherLibraryBookPath() {
        LibraryBook otherBook = libraryBook(13L);
        when(libraryBookRepository.findByIdAndUser(13L, currentUser)).thenReturn(Optional.of(otherBook));
        when(quoteRepository.findByIdAndLibraryBook(30L, otherBook)).thenReturn(Optional.empty());

        assertNotFound(() -> service.updateQuote(13L, 30L, new UpdateQuoteRequest()));
        verify(quoteRepository, never()).saveAndFlush(any());
    }

    @Test
    void cannotDeleteQuoteThroughAnotherLibraryBookPath() {
        LibraryBook otherBook = libraryBook(13L);
        when(libraryBookRepository.findByIdAndUser(13L, currentUser)).thenReturn(Optional.of(otherBook));
        when(quoteRepository.findByIdAndLibraryBook(30L, otherBook)).thenReturn(Optional.empty());

        assertNotFound(() -> service.deleteQuote(13L, 30L));
        verify(quoteRepository, never()).delete(any());
    }

    @Test
    void deletesOnlyQuoteBelongingToOwnedLibraryBook() {
        Quote quote = quote(30L, libraryBook, "Quote", "2026-10-14T09:00:00Z");
        when(libraryBookRepository.findByIdAndUser(12L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(quoteRepository.findByIdAndLibraryBook(30L, libraryBook)).thenReturn(Optional.of(quote));

        service.deleteQuote(12L, 30L);

        verify(quoteRepository).delete(quote);
        verify(libraryBookRepository, never()).delete(any());
    }

    @Test
    void cannotAccessAnotherUsersQuotes() {
        when(libraryBookRepository.findByIdAndUser(99L, currentUser)).thenReturn(Optional.empty());

        assertNotFound(() -> service.getQuotes(99L));
        verify(quoteRepository, never()).findAllByLibraryBookOrderByCreatedAtAsc(any());
    }

    private Quote quote(Long id, LibraryBook owner, String text, String createdAt) {
        Quote quote = new Quote();
        quote.setId(id);
        quote.setLibraryBook(owner);
        quote.setQuoteText(text);
        quote.setCreatedAt(Instant.parse(createdAt));
        return quote;
    }

    private LibraryBook libraryBook(Long id) {
        Book book = new Book();
        book.setId(100L + id);
        book.setTitle("Book " + id);
        LibraryBook owned = new LibraryBook();
        owned.setId(id);
        owned.setUser(currentUser);
        owned.setBook(book);
        owned.setStatus(ReadingStatus.TO_READ);
        owned.setCurrentPage(0);
        return owned;
    }

    private void assertNotFound(Runnable action) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
