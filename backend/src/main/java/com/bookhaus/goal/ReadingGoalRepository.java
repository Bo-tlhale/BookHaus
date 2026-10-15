package com.bookhaus.goal;

import com.bookhaus.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingGoalRepository extends JpaRepository<ReadingGoal, Long> {
    List<ReadingGoal> findAllByUserOrderByYearDesc(User user);

    Optional<ReadingGoal> findByUserAndYear(User user, Integer year);

    boolean existsByUserAndYear(User user, Integer year);
}
