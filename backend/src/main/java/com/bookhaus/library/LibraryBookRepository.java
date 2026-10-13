package com.bookhaus.library;

import com.bookhaus.book.Book;
import com.bookhaus.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {
    boolean existsByUserAndBook(User user, Book book);
}
