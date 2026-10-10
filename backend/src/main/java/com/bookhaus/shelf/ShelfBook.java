package com.bookhaus.shelf;

import com.bookhaus.library.LibraryBook;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "shelf_books",
    uniqueConstraints = @UniqueConstraint(columnNames = {"shelf_id", "library_book_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class ShelfBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shelf_id", nullable = false)
    private Shelf shelf;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "library_book_id", nullable = false)
    private LibraryBook libraryBook;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer position = 0;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    @PrePersist
    void onCreate() {
        addedAt = Instant.now();
    }
}
