package com.bookhaus.stats.dto;

import java.math.BigDecimal;
import java.util.List;

public record YearlyReadingStatsResponse(
        int year,
        long completedBooks,
        long pagesRead,
        BigDecimal averageRating,
        String mostReadGenre,
        List<GenreStatResponse> genres,
        ReadingGoalProgressResponse goal
) {
}
