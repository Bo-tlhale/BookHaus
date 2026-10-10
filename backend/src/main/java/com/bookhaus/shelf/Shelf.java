package com.bookhaus.shelf;

import com.bookhaus.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(
    name = "shelves",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"})
)
@Getter
@Setter
@NoArgsConstructor
public class Shelf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer position = 0;

    @OneToMany(mappedBy = "shelf", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private Set<ShelfBook> shelfBooks = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        normalize();
        createdAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        normalize();
    }

    private void normalize() {
        if (name != null) {
            name = name.trim().replaceAll("\\s+", " ");
        }
    }
}
