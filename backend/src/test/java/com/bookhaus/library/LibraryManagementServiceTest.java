package com.bookhaus.library;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhaus.book.AuthorRepository;
import com.bookhaus.book.Book;
import com.bookhaus.book.BookRepository;
import com.bookhaus.book.GenreRepository;
import com.bookhaus.library.dto.UpdateProgressRequest;
import com.bookhaus.library.dto.UpdateRatingRequest;
import com.bookhaus.library.dto.UpdateReadingStatusRequest;
import com.bookhaus.user.User;
import com.bookhaus.user.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class LibraryManagementServiceTest {
    @Mock UserService userService;
    @Mock BookRepository bookRepository;
    @Mock AuthorRepository authorRepository;
    @Mock GenreRepository genreRepository;
    @Mock LibraryBookRepository libraryBookRepository;
    private LibraryService service;
    private User currentUser;

    @BeforeEach
    void setUp() {
        service = new LibraryService(userService, bookRepository, authorRepository, genreRepository, libraryBookRepository);
        currentUser = new User();
        currentUser.setId(7L);
        currentUser.setEmail("reader@example.com");
        when(userService.getCurrentUserEntity()).thenReturn(currentUser);
    }

    @Test
    void listsOnlyCurrentUsersBooksAndSupportsStatusFilter() {
        LibraryBook first = libraryBook(1L, ReadingStatus.TO_READ, 0, 400);
        LibraryBook second = libraryBook(2L, ReadingStatus.CURRENTLY_READING, 100, 400);
        when(libraryBookRepository.findAllByUserOrderByAddedAtDesc(currentUser)).thenReturn(List.of(first, second));
        when(libraryBookRepository.findAllByUserAndStatusOrderByAddedAtDesc(currentUser, ReadingStatus.CURRENTLY_READING))
                .thenReturn(List.of(second));

        assertThat(service.getLibrary(null)).extracting(response -> response.libraryBookId())
                .containsExactly(1L, 2L);
        assertThat(service.getLibrary(ReadingStatus.CURRENTLY_READING)).singleElement()
                .satisfies(response -> {
                    assertThat(response.libraryBookId()).isEqualTo(2L);
                    assertThat(response.progressPercentage()).isEqualByComparingTo("25.0");
                });
    }

    @Test
    void doesNotExposeAnotherUsersLibraryBook() {
        when(libraryBookRepository.findByIdAndUser(99L, currentUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLibraryBook(99L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void currentlyReadingSetsStartedDateOnlyWhenAbsent() {
        LibraryBook libraryBook = libraryBook(1L, ReadingStatus.TO_READ, 0, 400);
        when(libraryBookRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(libraryBook));

        var response = service.updateStatus(1L, new UpdateReadingStatusRequest(ReadingStatus.CURRENTLY_READING));

        assertThat(response.readingStatus()).isEqualTo(ReadingStatus.CURRENTLY_READING);
        assertThat(response.startedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void completedSetsFinishedDateAndKnownPageCount() {
        LibraryBook libraryBook = libraryBook(1L, ReadingStatus.CURRENTLY_READING, 125, 400);
        when(libraryBookRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(libraryBook));

        var response = service.updateStatus(1L, new UpdateReadingStatusRequest(ReadingStatus.COMPLETED));

        assertThat(response.finishedDate()).isEqualTo(LocalDate.now());
        assertThat(response.currentPage()).isEqualTo(400);
        assertThat(response.progressPercentage()).isEqualByComparingTo("100.0");
    }

    @Test
    void dnfPreservesCurrentPage() {
        LibraryBook libraryBook = libraryBook(1L, ReadingStatus.CURRENTLY_READING, 125, 400);
        when(libraryBookRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(libraryBook));

        var response = service.updateStatus(1L, new UpdateReadingStatusRequest(ReadingStatus.DNF));

        assertThat(response.readingStatus()).isEqualTo(ReadingStatus.DNF);
        assertThat(response.currentPage()).isEqualTo(125);
    }

    @Test
    void progressPercentageIsNullWithoutPageCount() {
        LibraryBook libraryBook = libraryBook(1L, ReadingStatus.CURRENTLY_READING, 25, null);
        when(libraryBookRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(libraryBook));

        assertThat(service.getLibraryBook(1L).progressPercentage()).isNull();
    }

    @Test
    void completedBookProgressIsAlwaysOneHundredForKnownPageCount() {
        LibraryBook libraryBook = libraryBook(1L, ReadingStatus.COMPLETED, 10, 400);
        when(libraryBookRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(libraryBook));

        assertThat(service.getLibraryBook(1L).progressPercentage()).isEqualByComparingTo("100.0");
    }

    @Test
    void rejectsProgressAboveKnownPageCount() {
        LibraryBook libraryBook = libraryBook(1L, ReadingStatus.CURRENTLY_READING, 20, 400);
        when(libraryBookRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(libraryBook));

        assertThatThrownBy(() -> service.updateProgress(1L, new UpdateProgressRequest(401)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThat(libraryBook.getCurrentPage()).isEqualTo(20);
    }

    @Test
    void savesValidProgressWithoutChangingStatus() {
        LibraryBook libraryBook = libraryBook(1L, ReadingStatus.TO_READ, 0, 400);
        when(libraryBookRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(libraryBook));

        var response = service.updateProgress(1L, new UpdateProgressRequest(80));

        assertThat(response.currentPage()).isEqualTo(80);
        assertThat(response.progressPercentage()).isEqualByComparingTo("20.0");
        assertThat(response.readingStatus()).isEqualTo(ReadingStatus.TO_READ);
    }

    @Test
    void savesAndClearsPersonalRating() {
        LibraryBook libraryBook = libraryBook(1L, ReadingStatus.COMPLETED, 400, 400);
        when(libraryBookRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(libraryBook));

        assertThat(service.updateRating(1L, new UpdateRatingRequest(new BigDecimal("4.5"))).personalRating())
                .isEqualByComparingTo("4.5");
        assertThat(service.updateRating(1L, new UpdateRatingRequest(null)).personalRating()).isNull();
    }

    @Test
    void deletesOnlyOwnedLibraryBookAndNotSharedBook() {
        LibraryBook libraryBook = libraryBook(1L, ReadingStatus.TO_READ, 0, 400);
        when(libraryBookRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(libraryBook));

        service.removeBook(1L);

        verify(libraryBookRepository).delete(libraryBook);
        verify(bookRepository, never()).delete(libraryBook.getBook());
    }

    private LibraryBook libraryBook(Long id, ReadingStatus status, int currentPage, Integer pageCount) {
        Book book = new Book();
        book.setId(100L + id);
        book.setTitle("Book " + id);
        book.setPageCount(pageCount);
        LibraryBook libraryBook = new LibraryBook();
        libraryBook.setId(id);
        libraryBook.setUser(currentUser);
        libraryBook.setBook(book);
        libraryBook.setStatus(status);
        libraryBook.setCurrentPage(currentPage);
        return libraryBook;
    }
}
