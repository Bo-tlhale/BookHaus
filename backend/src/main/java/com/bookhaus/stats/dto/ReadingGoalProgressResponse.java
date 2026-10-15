package com.bookhaus.stats.dto;

import com.bookhaus.goal.dto.ReadingGoalResponse;
import java.math.BigDecimal;

public record ReadingGoalProgressResponse(
        Integer year,
        Integer targetBooks,
        long completedBooks,
        long remainingBooks,
        BigDecimal progressPercentage,
        boolean onPace
) {
    public static ReadingGoalProgressResponse from(ReadingGoalResponse response) {
        return new ReadingGoalProgressResponse(
                response.year(),
                response.targetBooks(),
                response.completedBooks(),
                response.remainingBooks(),
                response.progressPercentage(),
                response.onPace()
        );
    }
}
