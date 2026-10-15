package com.bookhaus.goal;

import com.bookhaus.goal.dto.CreateReadingGoalRequest;
import com.bookhaus.goal.dto.ReadingGoalResponse;
import com.bookhaus.goal.dto.UpdateReadingGoalRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/goals")
public class ReadingGoalController {
    private final ReadingGoalService readingGoalService;

    public ReadingGoalController(ReadingGoalService readingGoalService) {
        this.readingGoalService = readingGoalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReadingGoalResponse createGoal(@Valid @RequestBody CreateReadingGoalRequest request) {
        return readingGoalService.createGoal(request);
    }

    @GetMapping
    public List<ReadingGoalResponse> getGoals() {
        return readingGoalService.getGoals();
    }

    @GetMapping("/{year}")
    public ReadingGoalResponse getGoal(@PathVariable Integer year) {
        validateYear(year);
        return readingGoalService.getGoal(year);
    }

    @PatchMapping("/{year}")
    public ReadingGoalResponse updateGoal(
            @PathVariable Integer year,
            @Valid @RequestBody UpdateReadingGoalRequest request
    ) {
        validateYear(year);
        return readingGoalService.updateGoal(year, request);
    }

    @DeleteMapping("/{year}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGoal(@PathVariable Integer year) {
        validateYear(year);
        readingGoalService.deleteGoal(year);
    }

    private void validateYear(Integer year) {
        if (year < 1900 || year > 9999) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year must be between 1900 and 9999");
        }
    }
}
