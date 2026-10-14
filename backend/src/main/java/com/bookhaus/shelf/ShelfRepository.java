package com.bookhaus.shelf;

import com.bookhaus.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelfRepository extends JpaRepository<Shelf, Long> {
    List<Shelf> findAllByUserOrderByPositionAsc(User user);

    Optional<Shelf> findByIdAndUser(Long id, User user);

    boolean existsByUserAndNameIgnoreCase(User user, String name);

    boolean existsByUserAndNameIgnoreCaseAndIdNot(User user, String name, Long id);

    Optional<Shelf> findTopByUserOrderByPositionDesc(User user);
}
