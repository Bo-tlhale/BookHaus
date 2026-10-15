package com.bookhaus.stats.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReadingStatsResponse(
        long totalBooksInLibrary,
        long completedBooks,
        long currentlyReadingBooks,
        long wishlistBooks,
        long toReadBooks,
        long dnfBooks,
        long totalPagesRead,
        BigDecimal averageRating,
        String mostReadGenre,
        List<GenreStatResponse> genres,
        int currentYear,
        long booksCompletedThisYear,
        long pagesReadThisYear,
        ReadingGoalProgressResponse currentGoal
) {
}
