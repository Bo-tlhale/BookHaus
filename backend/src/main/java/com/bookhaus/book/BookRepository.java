package com.bookhaus.book;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByExternalProviderAndExternalBookId(String externalProvider, String externalBookId);

    Optional<Book> findFirstByIsbn10(String isbn10);

    Optional<Book> findFirstByIsbn13(String isbn13);

    Optional<Book> findFirstByTitleIgnoreCaseAndPublicationYear(String title, Integer publicationYear);
}
