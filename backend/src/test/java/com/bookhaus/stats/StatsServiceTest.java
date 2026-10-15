package com.bookhaus.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhaus.book.Book;
import com.bookhaus.book.Genre;
import com.bookhaus.goal.ReadingGoal;
import com.bookhaus.goal.ReadingGoalProgressCalculator;
import com.bookhaus.goal.ReadingGoalRepository;
import com.bookhaus.library.LibraryBook;
import com.bookhaus.library.LibraryBookRepository;
import com.bookhaus.library.ReadingStatus;
import com.bookhaus.user.User;
import com.bookhaus.user.UserService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);

    @Mock UserService userService;
    @Mock LibraryBookRepository libraryBookRepository;
    @Mock ReadingGoalRepository readingGoalRepository;
    private StatsService service;
    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(7L);
        currentUser.setEmail("reader@example.com");
        service = new StatsService(
                userService,
                libraryBookRepository,
                readingGoalRepository,
                new ReadingGoalProgressCalculator(FIXED_CLOCK),
                FIXED_CLOCK
        );
        when(userService.getCurrentUserEntity()).thenReturn(currentUser);
    }

    @Test
    void calculatesLibraryAndStatusCountsFromAuthenticatedUsersBooksOnly() {
        when(libraryBookRepository.findAllByUser(currentUser)).thenReturn(List.of(
                libraryBook(1L, ReadingStatus.COMPLETED, 300, 300, null),
                libraryBook(2L, ReadingStatus.CURRENTLY_READING, 400, 100, null),
                libraryBook(3L, ReadingStatus.WISHLIST, 200, 0, null),
                libraryBook(4L, ReadingStatus.TO_READ, 200, 0, null),
                libraryBook(5L, ReadingStatus.DNF, 200, 50, null)
        ));
        when(readingGoalRepository.findByUserAndYear(currentUser, 2026)).thenReturn(Optional.empty());

        var response = service.getStats();

        assertThat(response.totalBooksInLibrary()).isEqualTo(5);
        assertThat(response.completedBooks()).isEqualTo(1);
        assertThat(response.currentlyReadingBooks()).isEqualTo(1);
        assertThat(response.wishlistBooks()).isEqualTo(1);
        assertThat(response.toReadBooks()).isEqualTo(1);
        assertThat(response.dnfBooks()).isEqualTo(1);
        verify(libraryBookRepository).findAllByUser(currentUser);
    }

    @Test
    void calculatesTotalPagesWithoutDoubleCountingOrInventingUnknownCounts() {
        when(libraryBookRepository.findAllByUser(currentUser)).thenReturn(List.of(
                libraryBook(1L, ReadingStatus.COMPLETED, 300, 20, null),
                libraryBook(2L, ReadingStatus.COMPLETED, null, 200, null),
                libraryBook(3L, ReadingStatus.CURRENTLY_READING, 400, 100, null),
                libraryBook(4L, ReadingStatus.DNF, null, 80, null),
                libraryBook(5L, ReadingStatus.TO_READ, 200, 75, null)
        ));
        when(readingGoalRepository.findByUserAndYear(currentUser, 2026)).thenReturn(Optional.empty());

        assertThat(service.getStats().totalPagesRead()).isEqualTo(480);
    }

    @Test
    void averageRatingIgnoresUnratedBooks() {
        LibraryBook first = libraryBook(1L, ReadingStatus.COMPLETED, 300, 300, null);
        first.setPersonalRating(new BigDecimal("4.0"));
        LibraryBook second = libraryBook(2L, ReadingStatus.COMPLETED, 200, 200, null);
        second.setPersonalRating(new BigDecimal("5.0"));
        LibraryBook unrated = libraryBook(3L, ReadingStatus.TO_READ, 200, 0, null);
        when(libraryBookRepository.findAllByUser(currentUser)).thenReturn(List.of(first, second, unrated));
        when(readingGoalRepository.findByUserAndYear(currentUser, 2026)).thenReturn(Optional.empty());

        assertThat(service.getStats().averageRating()).isEqualByComparingTo("4.5");
    }

    @Test
    void averageRatingIsNullWhenNoBooksAreRated() {
        when(libraryBookRepository.findAllByUser(currentUser)).thenReturn(List.of(
                libraryBook(1L, ReadingStatus.TO_READ, 200, 0, null)));
        when(readingGoalRepository.findByUserAndYear(currentUser, 2026)).thenReturn(Optional.empty());

        assertThat(service.getStats().averageRating()).isNull();
    }

    @Test
    void yearlyPagesUseOnlyKnownPageCountsForBooksCompletedThatYear() {
        when(libraryBookRepository.findAllByUser(currentUser)).thenReturn(List.of(
                libraryBook(1L, ReadingStatus.COMPLETED, 300, 300, LocalDate.of(2026, 1, 10)),
                libraryBook(2L, ReadingStatus.COMPLETED, null, 200, LocalDate.of(2026, 2, 10)),
                libraryBook(3L, ReadingStatus.COMPLETED, 250, 250, LocalDate.of(2025, 12, 10)),
                libraryBook(4L, ReadingStatus.CURRENTLY_READING, 400, 100, null)
        ));
        when(readingGoalRepository.findByUserAndYear(currentUser, 2026)).thenReturn(Optional.empty());

        var response = service.getStats();

        assertThat(response.booksCompletedThisYear()).isEqualTo(2);
        assertThat(response.pagesReadThisYear()).isEqualTo(300);
    }

    @Test
    void countsCompletedBooksAcrossGenresAndBreaksTopGenreTiesAlphabetically() {
        LibraryBook first = libraryBook(1L, ReadingStatus.COMPLETED, 300, 300, null);
        addGenres(first, "Science Fiction", "Fantasy");
        LibraryBook second = libraryBook(2L, ReadingStatus.COMPLETED, 200, 200, null);
        addGenres(second, "Fantasy");
        LibraryBook third = libraryBook(3L, ReadingStatus.COMPLETED, 250, 250, null);
        addGenres(third, "Science Fiction", "Mystery");
        LibraryBook unfinished = libraryBook(4L, ReadingStatus.CURRENTLY_READING, 400, 100, null);
        addGenres(unfinished, "Mystery");
        when(libraryBookRepository.findAllByUser(currentUser)).thenReturn(List.of(first, second, third, unfinished));
        when(readingGoalRepository.findByUserAndYear(currentUser, 2026)).thenReturn(Optional.empty());

        var response = service.getStats();

        assertThat(response.genres()).extracting(item -> item.genre() + ":" + item.completedBookCount())
                .containsExactly("Fantasy:2", "Science Fiction:2", "Mystery:1");
        assertThat(response.mostReadGenre()).isEqualTo("Fantasy");
    }

    @Test
    void yearlyEndpointRestrictsBooksPagesRatingsAndGenresToRequestedCompletionYear() {
        LibraryBook included = libraryBook(1L, ReadingStatus.COMPLETED, 300, 300, LocalDate.of(2025, 5, 1));
        included.setPersonalRating(new BigDecimal("4.5"));
        addGenres(included, "Fantasy");
        LibraryBook excluded = libraryBook(2L, ReadingStatus.COMPLETED, 200, 200, LocalDate.of(2026, 5, 1));
        excluded.setPersonalRating(new BigDecimal("2.0"));
        addGenres(excluded, "Mystery");
        when(libraryBookRepository.findAllByUser(currentUser)).thenReturn(List.of(included, excluded));
        when(readingGoalRepository.findByUserAndYear(currentUser, 2025)).thenReturn(Optional.of(goal(2025, 1)));

        var response = service.getYearlyStats(2025);

        assertThat(response.completedBooks()).isEqualTo(1);
        assertThat(response.pagesRead()).isEqualTo(300);
        assertThat(response.averageRating()).isEqualByComparingTo("4.5");
        assertThat(response.mostReadGenre()).isEqualTo("Fantasy");
        assertThat(response.goal().completedBooks()).isEqualTo(1);
    }

    private ReadingGoal goal(int year, int target) {
        ReadingGoal goal = new ReadingGoal();
        goal.setUser(currentUser);
        goal.setYear(year);
        goal.setTargetBooks(target);
        return goal;
    }

    private LibraryBook libraryBook(
            Long id,
            ReadingStatus status,
            Integer pageCount,
            int currentPage,
            LocalDate finishedDate
    ) {
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
        libraryBook.setFinishedDate(finishedDate);
        return libraryBook;
    }

    private void addGenres(LibraryBook libraryBook, String... names) {
        for (String name : names) {
            Genre genre = new Genre();
            genre.setName(name);
            libraryBook.getBook().getGenres().add(genre);
        }
    }
}
