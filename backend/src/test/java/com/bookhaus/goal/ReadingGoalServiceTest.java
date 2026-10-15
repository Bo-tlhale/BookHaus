package com.bookhaus.goal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhaus.book.Book;
import com.bookhaus.goal.dto.CreateReadingGoalRequest;
import com.bookhaus.goal.dto.UpdateReadingGoalRequest;
import com.bookhaus.library.LibraryBook;
import com.bookhaus.library.LibraryBookRepository;
import com.bookhaus.library.ReadingStatus;
import com.bookhaus.user.User;
import com.bookhaus.user.UserService;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ReadingGoalServiceTest {
    @Mock UserService userService;
    @Mock ReadingGoalRepository readingGoalRepository;
    @Mock LibraryBookRepository libraryBookRepository;
    private ReadingGoalService service;
    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(7L);
        currentUser.setEmail("reader@example.com");
        var calculator = new ReadingGoalProgressCalculator(
                Clock.fixed(Instant.parse("2026-07-01T12:00:00Z"), ZoneOffset.UTC));
        service = new ReadingGoalService(userService, readingGoalRepository, libraryBookRepository, calculator);
        when(userService.getCurrentUserEntity()).thenReturn(currentUser);
    }

    @Test
    void createsGoalAndCalculatesProgressWithoutPersistingDerivedFields() {
        when(readingGoalRepository.existsByUserAndYear(currentUser, 2026)).thenReturn(false);
        when(readingGoalRepository.saveAndFlush(any(ReadingGoal.class))).thenAnswer(invocation -> {
            ReadingGoal goal = invocation.getArgument(0);
            goal.setId(10L);
            goal.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
            return goal;
        });
        when(libraryBookRepository.findAllByUser(currentUser)).thenReturn(List.of(
                libraryBook(1L, ReadingStatus.COMPLETED, LocalDate.of(2026, 2, 1)),
                libraryBook(2L, ReadingStatus.COMPLETED, LocalDate.of(2025, 12, 31))
        ));

        var response = service.createGoal(new CreateReadingGoalRequest(2026, 4));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.completedBooks()).isEqualTo(1);
        assertThat(response.remainingBooks()).isEqualTo(3);
        assertThat(response.progressPercentage()).isEqualByComparingTo("25.0");
    }

    @Test
    void rejectsDuplicateGoalYear() {
        when(readingGoalRepository.existsByUserAndYear(currentUser, 2026)).thenReturn(true);

        assertConflict(() -> service.createGoal(new CreateReadingGoalRequest(2026, 12)));
        verify(readingGoalRepository, never()).saveAndFlush(any());
    }

    @Test
    void listsOnlyCurrentUsersGoalsInRepositoryOrder() {
        when(readingGoalRepository.findAllByUserOrderByYearDesc(currentUser))
                .thenReturn(List.of(goal(2L, 2027, 10), goal(1L, 2026, 4)));
        when(libraryBookRepository.findAllByUser(currentUser)).thenReturn(List.of(
                libraryBook(1L, ReadingStatus.COMPLETED, LocalDate.of(2026, 2, 1)),
                libraryBook(2L, ReadingStatus.COMPLETED, LocalDate.of(2026, 3, 1))
        ));

        var responses = service.getGoals();

        assertThat(responses).extracting(response -> response.year()).containsExactly(2027, 2026);
        assertThat(responses.get(1).completedBooks()).isEqualTo(2);
        assertThat(responses.get(1).progressPercentage()).isEqualByComparingTo("50.0");
    }

    @Test
    void getsGoalByYearForCurrentUser() {
        ReadingGoal goal = goal(1L, 2026, 5);
        when(readingGoalRepository.findByUserAndYear(currentUser, 2026)).thenReturn(Optional.of(goal));
        when(libraryBookRepository.findAllByUser(currentUser)).thenReturn(List.of());

        assertThat(service.getGoal(2026).id()).isEqualTo(1L);
    }

    @Test
    void updatesOnlyOwnedGoalTarget() {
        ReadingGoal goal = goal(1L, 2026, 5);
        when(readingGoalRepository.findByUserAndYear(currentUser, 2026)).thenReturn(Optional.of(goal));
        when(readingGoalRepository.saveAndFlush(goal)).thenReturn(goal);
        when(libraryBookRepository.findAllByUser(currentUser)).thenReturn(List.of());

        var response = service.updateGoal(2026, new UpdateReadingGoalRequest(20));

        assertThat(response.targetBooks()).isEqualTo(20);
        assertThat(goal.getUser()).isSameAs(currentUser);
    }

    @Test
    void deletesOnlyOwnedGoal() {
        ReadingGoal goal = goal(1L, 2026, 5);
        when(readingGoalRepository.findByUserAndYear(currentUser, 2026)).thenReturn(Optional.of(goal));

        service.deleteGoal(2026);

        verify(readingGoalRepository).delete(goal);
        verify(libraryBookRepository, never()).delete(any());
    }

    @Test
    void anotherUsersGoalIsInaccessible() {
        when(readingGoalRepository.findByUserAndYear(currentUser, 2026)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getGoal(2026))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(libraryBookRepository, never()).findAllByUser(any());
    }

    private ReadingGoal goal(Long id, int year, int targetBooks) {
        ReadingGoal goal = new ReadingGoal();
        goal.setId(id);
        goal.setUser(currentUser);
        goal.setYear(year);
        goal.setTargetBooks(targetBooks);
        goal.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
        return goal;
    }

    private LibraryBook libraryBook(Long id, ReadingStatus status, LocalDate finishedDate) {
        Book book = new Book();
        book.setId(100L + id);
        book.setTitle("Book " + id);
        LibraryBook libraryBook = new LibraryBook();
        libraryBook.setId(id);
        libraryBook.setUser(currentUser);
        libraryBook.setBook(book);
        libraryBook.setStatus(status);
        libraryBook.setCurrentPage(0);
        libraryBook.setFinishedDate(finishedDate);
        return libraryBook;
    }

    private void assertConflict(Runnable action) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }
}
