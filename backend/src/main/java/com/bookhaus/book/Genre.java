package com.bookhaus.book;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @PrePersist
    @PreUpdate
    void normalizeName() {
        if (name != null) {
            name = name.trim().replaceAll("\\s+", " ");
        }
    }
}
