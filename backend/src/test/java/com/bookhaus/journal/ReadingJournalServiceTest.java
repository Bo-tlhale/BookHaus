package com.bookhaus.journal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhaus.book.Book;
import com.bookhaus.journal.dto.UpsertReadingJournalRequest;
import com.bookhaus.library.LibraryBook;
import com.bookhaus.library.LibraryBookRepository;
import com.bookhaus.library.ReadingStatus;
import com.bookhaus.user.User;
import com.bookhaus.user.UserService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ReadingJournalServiceTest {
    @Mock UserService userService;
    @Mock LibraryBookRepository libraryBookRepository;
    @Mock ReadingJournalRepository readingJournalRepository;
    private ReadingJournalService service;
    private User currentUser;
    private LibraryBook libraryBook;

    @BeforeEach
    void setUp() {
        service = new ReadingJournalService(userService, libraryBookRepository, readingJournalRepository);
        currentUser = new User();
        currentUser.setId(7L);
        currentUser.setEmail("reader@example.com");
        libraryBook = libraryBook(12L);
        when(userService.getCurrentUserEntity()).thenReturn(currentUser);
    }

    @Test
    void createsPartialJournalForOwnedLibraryBookWithoutRewritingText() {
        when(libraryBookRepository.findByIdAndUser(12L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(readingJournalRepository.findByLibraryBook(libraryBook)).thenReturn(Optional.empty());
        when(readingJournalRepository.saveAndFlush(any(ReadingJournal.class))).thenAnswer(invocation -> {
            ReadingJournal journal = invocation.getArgument(0);
            journal.setId(20L);
            journal.setUpdatedAt(Instant.parse("2026-10-14T10:00:00Z"));
            return journal;
        });

        var response = service.upsertJournal(12L,
                new UpsertReadingJournalRequest(null, "  It made me pause.\nA lot.  ", null, null));

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.libraryBookId()).isEqualTo(12L);
        assertThat(response.feelings()).isEqualTo("  It made me pause.\nA lot.  ");
        assertThat(response.aboutBook()).isNull();
    }

    @Test
    void upsertUpdatesExistingJournalInsteadOfCreatingDuplicate() {
        ReadingJournal existing = journal(20L);
        when(libraryBookRepository.findByIdAndUser(12L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(readingJournalRepository.findByLibraryBook(libraryBook)).thenReturn(Optional.of(existing));
        when(readingJournalRepository.saveAndFlush(existing)).thenReturn(existing);

        var response = service.upsertJournal(12L,
                new UpsertReadingJournalRequest("About", "Feelings", "Reflection", "Lessons"));

        assertThat(response.id()).isEqualTo(20L);
        assertThat(existing.getReflection()).isEqualTo("Reflection");
        verify(readingJournalRepository).saveAndFlush(existing);
    }

    @Test
    void retrievesExistingJournal() {
        ReadingJournal existing = journal(20L);
        existing.setReflection("Keep the original\nformat.");
        when(libraryBookRepository.findByIdAndUser(12L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(readingJournalRepository.findByLibraryBook(libraryBook)).thenReturn(Optional.of(existing));

        assertThat(service.getJournal(12L).reflection()).isEqualTo("Keep the original\nformat.");
    }

    @Test
    void returnsNotFoundWhenJournalDoesNotExist() {
        when(libraryBookRepository.findByIdAndUser(12L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(readingJournalRepository.findByLibraryBook(libraryBook)).thenReturn(Optional.empty());

        assertNotFound(() -> service.getJournal(12L));
    }

    @Test
    void cannotAccessAnotherUsersLibraryBookOrJournal() {
        when(libraryBookRepository.findByIdAndUser(99L, currentUser)).thenReturn(Optional.empty());

        assertNotFound(() -> service.getJournal(99L));
        verify(readingJournalRepository, never()).findByLibraryBook(any());
    }

    private ReadingJournal journal(Long id) {
        ReadingJournal journal = new ReadingJournal();
        journal.setId(id);
        journal.setLibraryBook(libraryBook);
        journal.setUpdatedAt(Instant.parse("2026-10-14T09:00:00Z"));
        return journal;
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
