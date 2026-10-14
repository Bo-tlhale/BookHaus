package com.bookhaus.shelf.dto;

import com.bookhaus.shelf.Shelf;
import java.time.Instant;
import java.util.List;

public record ShelfResponse(
        Long shelfId,
        String name,
        Integer position,
        Instant createdAt,
        int bookCount,
        List<ShelfBookResponse> books
) {
    public static ShelfResponse summary(Shelf shelf) {
        return new ShelfResponse(
                shelf.getId(), shelf.getName(), shelf.getPosition(), shelf.getCreatedAt(),
                shelf.getShelfBooks().size(), List.of()
        );
    }

    public static ShelfResponse details(Shelf shelf, List<ShelfBookResponse> books) {
        return new ShelfResponse(
                shelf.getId(), shelf.getName(), shelf.getPosition(), shelf.getCreatedAt(), books.size(), books
        );
    }
}
