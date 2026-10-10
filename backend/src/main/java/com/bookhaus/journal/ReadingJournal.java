package com.bookhaus.journal;

import com.bookhaus.library.LibraryBook;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "reading_journals")
@Getter
@Setter
@NoArgsConstructor
public class ReadingJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "library_book_id", nullable = false, unique = true)
    private LibraryBook libraryBook;

    @Column(name = "about_book", columnDefinition = "TEXT")
    private String aboutBook;

    @Column(columnDefinition = "TEXT")
    private String feelings;

    @Column(columnDefinition = "TEXT")
    private String reflection;

    @Column(name = "lessons_learned", columnDefinition = "TEXT")
    private String lessonsLearned;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }
}
