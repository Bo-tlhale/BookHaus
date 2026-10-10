package com.bookhaus.goal;

import com.bookhaus.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "reading_goals",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "year"})
)
@Getter
@Setter
@NoArgsConstructor
public class ReadingGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Min(1900)
    @Column(nullable = false)
    private Integer year;

    @NotNull
    @Positive
    @Column(name = "target_books", nullable = false)
    private Integer targetBooks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
