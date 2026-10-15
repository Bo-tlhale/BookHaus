package com.bookhaus.stats;

import com.bookhaus.goal.ReadingGoalProgressCalculator;
import com.bookhaus.goal.ReadingGoalRepository;
import com.bookhaus.library.LibraryBook;
import com.bookhaus.library.LibraryBookRepository;
import com.bookhaus.library.ReadingStatus;
import com.bookhaus.stats.dto.GenreStatResponse;
import com.bookhaus.stats.dto.ReadingGoalProgressResponse;
import com.bookhaus.stats.dto.ReadingStatsResponse;
import com.bookhaus.stats.dto.YearlyReadingStatsResponse;
import com.bookhaus.user.User;
import com.bookhaus.user.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {
    private static final Comparator<GenreStatResponse> GENRE_ORDER = Comparator
            .comparingLong(GenreStatResponse::completedBookCount).reversed()
            .thenComparing(GenreStatResponse::genre, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(GenreStatResponse::genre);

    private final UserService userService;
    private final LibraryBookRepository libraryBookRepository;
    private final ReadingGoalRepository readingGoalRepository;
    private final ReadingGoalProgressCalculator progressCalculator;
    private final Clock clock;

    public StatsService(
            UserService userService,
            LibraryBookRepository libraryBookRepository,
            ReadingGoalRepository readingGoalRepository,
            ReadingGoalProgressCalculator progressCalculator,
            Clock clock
    ) {
        this.userService = userService;
        this.libraryBookRepository = libraryBookRepository;
        this.readingGoalRepository = readingGoalRepository;
        this.progressCalculator = progressCalculator;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ReadingStatsResponse getStats() {
        User user = userService.getCurrentUserEntity();
        List<LibraryBook> books = libraryBookRepository.findAllByUser(user);
        int currentYear = LocalDate.now(clock).getYear();
        List<LibraryBook> completedBooks = completedBooks(books);
        List<LibraryBook> completedThisYear = completedInYear(books, currentYear);
        List<GenreStatResponse> genres = genreStats(completedBooks);

        return new ReadingStatsResponse(
                books.size(),
                countStatus(books, ReadingStatus.COMPLETED),
                countStatus(books, ReadingStatus.CURRENTLY_READING),
                countStatus(books, ReadingStatus.WISHLIST),
                countStatus(books, ReadingStatus.TO_READ),
                countStatus(books, ReadingStatus.DNF),
                books.stream().mapToLong(this::pagesRead).sum(),
                averageRating(books),
                mostReadGenre(genres),
                genres,
                currentYear,
                completedThisYear.size(),
                completedThisYear.stream().mapToLong(this::completedPageCount).sum(),
                goalProgress(user, currentYear, completedThisYear.size())
        );
    }

    @Transactional(readOnly = true)
    public YearlyReadingStatsResponse getYearlyStats(Integer year) {
        User user = userService.getCurrentUserEntity();
        List<LibraryBook> completed = completedInYear(libraryBookRepository.findAllByUser(user), year);
        List<GenreStatResponse> genres = genreStats(completed);
        return new YearlyReadingStatsResponse(
                year,
                completed.size(),
                completed.stream().mapToLong(this::completedPageCount).sum(),
                averageRating(completed),
                mostReadGenre(genres),
                genres,
                goalProgress(user, year, completed.size())
        );
    }

    private long countStatus(List<LibraryBook> books, ReadingStatus status) {
        return books.stream().filter(book -> book.getStatus() == status).count();
    }

    private List<LibraryBook> completedBooks(List<LibraryBook> books) {
        return books.stream().filter(book -> book.getStatus() == ReadingStatus.COMPLETED).toList();
    }

    private List<LibraryBook> completedInYear(List<LibraryBook> books, int year) {
        return books.stream()
                .filter(book -> book.getStatus() == ReadingStatus.COMPLETED)
                .filter(book -> book.getFinishedDate() != null && book.getFinishedDate().getYear() == year)
                .toList();
    }

    private long pagesRead(LibraryBook libraryBook) {
        return switch (libraryBook.getStatus()) {
            case COMPLETED -> completedPageCount(libraryBook);
            case CURRENTLY_READING, DNF -> currentPageContribution(libraryBook);
            case WISHLIST, TO_READ -> 0;
        };
    }

    private long completedPageCount(LibraryBook libraryBook) {
        Integer pageCount = libraryBook.getBook().getPageCount();
        return pageCount == null || pageCount <= 0 ? 0 : pageCount;
    }

    private long currentPageContribution(LibraryBook libraryBook) {
        int currentPage = libraryBook.getCurrentPage() == null ? 0 : Math.max(0, libraryBook.getCurrentPage());
        Integer pageCount = libraryBook.getBook().getPageCount();
        return pageCount == null ? currentPage : Math.min(currentPage, pageCount);
    }

    private BigDecimal averageRating(List<LibraryBook> books) {
        List<BigDecimal> ratings = books.stream()
                .map(LibraryBook::getPersonalRating)
                .filter(rating -> rating != null)
                .toList();
        if (ratings.isEmpty()) {
            return null;
        }
        BigDecimal total = ratings.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(ratings.size()), 2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private List<GenreStatResponse> genreStats(List<LibraryBook> completedBooks) {
        Map<String, Long> counts = new HashMap<>();
        completedBooks.forEach(libraryBook -> libraryBook.getBook().getGenres().forEach(genre ->
                counts.merge(genre.getName(), 1L, Long::sum)));
        List<GenreStatResponse> result = new ArrayList<>();
        counts.forEach((genre, count) -> result.add(new GenreStatResponse(genre, count)));
        result.sort(GENRE_ORDER);
        return List.copyOf(result);
    }

    private String mostReadGenre(List<GenreStatResponse> genres) {
        return genres.isEmpty() ? null : genres.getFirst().genre();
    }

    private ReadingGoalProgressResponse goalProgress(User user, int year, long completedBooks) {
        return readingGoalRepository.findByUserAndYear(user, year)
                .map(goal -> ReadingGoalProgressResponse.from(progressCalculator.calculate(goal, completedBooks)))
                .orElse(null);
    }
}
