package com.bookhaus.shelf;

import com.bookhaus.library.LibraryBook;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelfBookRepository extends JpaRepository<ShelfBook, Long> {
    List<ShelfBook> findAllByShelfOrderByPositionAsc(Shelf shelf);

    boolean existsByShelfAndLibraryBook(Shelf shelf, LibraryBook libraryBook);

    Optional<ShelfBook> findByShelfAndLibraryBook(Shelf shelf, LibraryBook libraryBook);

    Optional<ShelfBook> findTopByShelfOrderByPositionDesc(Shelf shelf);
}
