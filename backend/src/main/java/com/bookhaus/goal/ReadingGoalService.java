package com.bookhaus.goal;

import com.bookhaus.goal.dto.CreateReadingGoalRequest;
import com.bookhaus.goal.dto.ReadingGoalResponse;
import com.bookhaus.goal.dto.UpdateReadingGoalRequest;
import com.bookhaus.library.LibraryBook;
import com.bookhaus.library.LibraryBookRepository;
import com.bookhaus.library.ReadingStatus;
import com.bookhaus.user.User;
import com.bookhaus.user.UserService;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReadingGoalService {
    private final UserService userService;
    private final ReadingGoalRepository readingGoalRepository;
    private final LibraryBookRepository libraryBookRepository;
    private final ReadingGoalProgressCalculator progressCalculator;

    public ReadingGoalService(
            UserService userService,
            ReadingGoalRepository readingGoalRepository,
            LibraryBookRepository libraryBookRepository,
            ReadingGoalProgressCalculator progressCalculator
    ) {
        this.userService = userService;
        this.readingGoalRepository = readingGoalRepository;
        this.libraryBookRepository = libraryBookRepository;
        this.progressCalculator = progressCalculator;
    }

    @Transactional
    public ReadingGoalResponse createGoal(CreateReadingGoalRequest request) {
        User user = userService.getCurrentUserEntity();
        if (readingGoalRepository.existsByUserAndYear(user, request.year())) {
            throw duplicateGoal();
        }
        ReadingGoal goal = new ReadingGoal();
        goal.setUser(user);
        goal.setYear(request.year());
        goal.setTargetBooks(request.targetBooks());
        try {
            return response(readingGoalRepository.saveAndFlush(goal), user);
        } catch (DataIntegrityViolationException exception) {
            throw duplicateGoal();
        }
    }

    @Transactional(readOnly = true)
    public List<ReadingGoalResponse> getGoals() {
        User user = userService.getCurrentUserEntity();
        List<LibraryBook> libraryBooks = libraryBookRepository.findAllByUser(user);
        return readingGoalRepository.findAllByUserOrderByYearDesc(user).stream()
                .map(goal -> response(goal, libraryBooks))
                .toList();
    }

    @Transactional(readOnly = true)
    public ReadingGoalResponse getGoal(Integer year) {
        User user = userService.getCurrentUserEntity();
        return response(findGoal(user, year), user);
    }

    @Transactional
    public ReadingGoalResponse updateGoal(Integer year, UpdateReadingGoalRequest request) {
        User user = userService.getCurrentUserEntity();
        ReadingGoal goal = findGoal(user, year);
        goal.setTargetBooks(request.targetBooks());
        return response(readingGoalRepository.saveAndFlush(goal), user);
    }

    @Transactional
    public void deleteGoal(Integer year) {
        User user = userService.getCurrentUserEntity();
        readingGoalRepository.delete(findGoal(user, year));
    }

    private ReadingGoal findGoal(User user, Integer year) {
        return readingGoalRepository.findByUserAndYear(user, year)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reading goal not found"));
    }

    private ReadingGoalResponse response(ReadingGoal goal, User user) {
        return response(goal, libraryBookRepository.findAllByUser(user));
    }

    private ReadingGoalResponse response(ReadingGoal goal, List<LibraryBook> libraryBooks) {
        long completedBooks = libraryBooks.stream()
                .filter(book -> book.getStatus() == ReadingStatus.COMPLETED)
                .filter(book -> book.getFinishedDate() != null && book.getFinishedDate().getYear() == goal.getYear())
                .count();
        return progressCalculator.calculate(goal, completedBooks);
    }

    private ResponseStatusException duplicateGoal() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "A reading goal already exists for this year");
    }
}
