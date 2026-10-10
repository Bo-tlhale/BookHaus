package com.bookhaus.book;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "authors")
@Getter
@Setter
@NoArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @PrePersist
    @PreUpdate
    void normalizeName() {
        if (name != null) {
            name = name.trim().replaceAll("\\s+", " ");
        }
    }
}
