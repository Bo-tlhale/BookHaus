package com.bookhaus.library;

import com.bookhaus.book.Book;
import com.bookhaus.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {
    boolean existsByUserAndBook(User user, Book book);

    List<LibraryBook> findAllByUserOrderByAddedAtDesc(User user);

    List<LibraryBook> findAllByUserAndStatusOrderByAddedAtDesc(User user, ReadingStatus status);

    @EntityGraph(attributePaths = {"book", "book.genres"})
    List<LibraryBook> findAllByUser(User user);

    Optional<LibraryBook> findByIdAndUser(Long id, User user);
}
