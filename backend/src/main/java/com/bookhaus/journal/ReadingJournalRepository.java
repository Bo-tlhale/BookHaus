package com.bookhaus.journal;

import com.bookhaus.library.LibraryBook;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingJournalRepository extends JpaRepository<ReadingJournal, Long> {
    Optional<ReadingJournal> findByLibraryBook(LibraryBook libraryBook);
}
