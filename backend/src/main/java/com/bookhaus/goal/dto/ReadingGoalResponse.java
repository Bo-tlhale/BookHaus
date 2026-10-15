package com.bookhaus.goal.dto;

import com.bookhaus.goal.ReadingGoal;
import java.math.BigDecimal;
import java.time.Instant;

public record ReadingGoalResponse(
        Long id,
        Integer year,
        Integer targetBooks,
        long completedBooks,
        long remainingBooks,
        BigDecimal progressPercentage,
        boolean onPace,
        Instant createdAt
) {
    public static ReadingGoalResponse from(
            ReadingGoal goal,
            long completedBooks,
            long remainingBooks,
            BigDecimal progressPercentage,
            boolean onPace
    ) {
        return new ReadingGoalResponse(
                goal.getId(),
                goal.getYear(),
                goal.getTargetBooks(),
                completedBooks,
                remainingBooks,
                progressPercentage,
                onPace,
                goal.getCreatedAt()
        );
    }
}
