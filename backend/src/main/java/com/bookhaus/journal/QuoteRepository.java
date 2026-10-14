package com.bookhaus.journal;

import com.bookhaus.library.LibraryBook;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findAllByLibraryBookOrderByCreatedAtAsc(LibraryBook libraryBook);

    Optional<Quote> findByIdAndLibraryBook(Long id, LibraryBook libraryBook);
}
