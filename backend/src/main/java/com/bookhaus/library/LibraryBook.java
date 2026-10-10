package com.bookhaus.library;

import com.bookhaus.book.Book;
import com.bookhaus.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(
    name = "library_books",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class LibraryBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReadingStatus status;

    @NotNull
    @Min(0)
    @Column(name = "current_page", nullable = false)
    private Integer currentPage = 0;

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    @Column(name = "personal_rating", precision = 2, scale = 1)
    private BigDecimal personalRating;

    @Column(name = "started_date")
    private LocalDate startedDate;

    @Column(name = "finished_date")
    private LocalDate finishedDate;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    @PrePersist
    void onCreate() {
        addedAt = Instant.now();
    }
}
