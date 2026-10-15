package com.bookhaus.goal;

import com.bookhaus.goal.dto.ReadingGoalResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class ReadingGoalProgressCalculator {
    private final Clock clock;

    public ReadingGoalProgressCalculator(Clock clock) {
        this.clock = clock;
    }

    public ReadingGoalResponse calculate(ReadingGoal goal, long completedBooks) {
        long remainingBooks = Math.max(0, goal.getTargetBooks() - completedBooks);
        BigDecimal percentage = BigDecimal.valueOf(completedBooks)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(goal.getTargetBooks()), 1, RoundingMode.HALF_UP);
        return ReadingGoalResponse.from(
                goal,
                completedBooks,
                remainingBooks,
                percentage,
                isOnPace(goal.getYear(), goal.getTargetBooks(), completedBooks)
        );
    }

    private boolean isOnPace(int goalYear, int targetBooks, long completedBooks) {
        LocalDate today = LocalDate.now(clock);
        if (goalYear < today.getYear()) {
            return completedBooks >= targetBooks;
        }
        if (goalYear > today.getYear()) {
            return true;
        }
        return completedBooks * today.lengthOfYear() >= (long) targetBooks * today.getDayOfYear();
    }
}
