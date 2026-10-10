package com.bookhaus.journal;

import com.bookhaus.library.LibraryBook;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "quotes")
@Getter
@Setter
@NoArgsConstructor
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "library_book_id", nullable = false)
    private LibraryBook libraryBook;

    @NotBlank
    @Column(name = "quote_text", nullable = false, columnDefinition = "TEXT")
    private String quoteText;

    @Positive
    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (quoteText != null) {
            quoteText = quoteText.trim();
        }
        createdAt = Instant.now();
    }
}
