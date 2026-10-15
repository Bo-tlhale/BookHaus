package com.bookhaus.goal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ReadingGoalProgressCalculatorTest {
    private final ReadingGoalProgressCalculator calculator = new ReadingGoalProgressCalculator(
            Clock.fixed(Instant.parse("2026-07-01T12:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void currentYearGoalIsOnPaceWhenActualProgressMeetsElapsedProgress() {
        var response = calculator.calculate(goal(2026, 4), 2);

        assertThat(response.progressPercentage()).isEqualByComparingTo("50.0");
        assertThat(response.onPace()).isTrue();
    }

    @Test
    void currentYearGoalIsBehindWhenActualProgressIsBelowElapsedProgress() {
        assertThat(calculator.calculate(goal(2026, 12), 2).onPace()).isFalse();
    }

    @Test
    void pastYearIsOnPaceOnlyWhenTargetWasUltimatelyMet() {
        assertThat(calculator.calculate(goal(2025, 10), 10).onPace()).isTrue();
        assertThat(calculator.calculate(goal(2025, 10), 9).onPace()).isFalse();
    }

    @Test
    void futureYearIsOnPaceBeforeProgressIsExpected() {
        assertThat(calculator.calculate(goal(2027, 10), 0).onPace()).isTrue();
    }

    private ReadingGoal goal(int year, int targetBooks) {
        ReadingGoal goal = new ReadingGoal();
        goal.setYear(year);
        goal.setTargetBooks(targetBooks);
        return goal;
    }
}
