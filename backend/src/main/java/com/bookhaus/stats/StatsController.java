package com.bookhaus.stats;

import com.bookhaus.stats.dto.ReadingStatsResponse;
import com.bookhaus.stats.dto.YearlyReadingStatsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ReadingStatsResponse getStats() {
        return statsService.getStats();
    }

    @GetMapping("/year/{year}")
    public YearlyReadingStatsResponse getYearlyStats(@PathVariable Integer year) {
        if (year < 1900 || year > 9999) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year must be between 1900 and 9999");
        }
        return statsService.getYearlyStats(year);
    }
}
