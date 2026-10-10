package com.bookhaus.book;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String title;

    @ManyToMany
    @JoinTable(
        name = "book_authors",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new LinkedHashSet<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 10)
    private String isbn10;

    @Column(length = 13)
    private String isbn13;

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @ManyToMany
    @JoinTable(
        name = "book_genres",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new LinkedHashSet<>();

    @Column(length = 255)
    private String publisher;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Positive
    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "external_provider", length = 100)
    private String externalProvider;

    @Column(name = "external_book_id", length = 255)
    private String externalBookId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
