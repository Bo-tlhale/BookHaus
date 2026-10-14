package com.bookhaus.shelf;

import com.bookhaus.shelf.dto.AddShelfBookRequest;
import com.bookhaus.shelf.dto.CreateShelfRequest;
import com.bookhaus.shelf.dto.RenameShelfRequest;
import com.bookhaus.shelf.dto.ReorderShelfBooksRequest;
import com.bookhaus.shelf.dto.ReorderShelvesRequest;
import com.bookhaus.shelf.dto.ShelfBookResponse;
import com.bookhaus.shelf.dto.ShelfResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shelves")
public class ShelfController {
    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @GetMapping
    public List<ShelfResponse> getShelves() {
        return shelfService.getShelves();
    }

    @GetMapping("/{shelfId}")
    public ShelfResponse getShelf(@PathVariable Long shelfId) {
        return shelfService.getShelf(shelfId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShelfResponse createShelf(@Valid @RequestBody CreateShelfRequest request) {
        return shelfService.createShelf(request);
    }

    @PatchMapping("/{shelfId}")
    public ShelfResponse renameShelf(@PathVariable Long shelfId, @Valid @RequestBody RenameShelfRequest request) {
        return shelfService.renameShelf(shelfId, request);
    }

    @DeleteMapping("/{shelfId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShelf(@PathVariable Long shelfId) {
        shelfService.deleteShelf(shelfId);
    }

    @PostMapping("/{shelfId}/books")
    @ResponseStatus(HttpStatus.CREATED)
    public ShelfBookResponse addBook(
            @PathVariable Long shelfId,
            @Valid @RequestBody AddShelfBookRequest request
    ) {
        return shelfService.addBook(shelfId, request);
    }

    @DeleteMapping("/{shelfId}/books/{libraryBookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBook(@PathVariable Long shelfId, @PathVariable Long libraryBookId) {
        shelfService.removeBook(shelfId, libraryBookId);
    }

    @PatchMapping("/reorder")
    public List<ShelfResponse> reorderShelves(@Valid @RequestBody ReorderShelvesRequest request) {
        return shelfService.reorderShelves(request);
    }

    @PatchMapping("/{shelfId}/books/reorder")
    public ShelfResponse reorderShelfBooks(
            @PathVariable Long shelfId,
            @Valid @RequestBody ReorderShelfBooksRequest request
    ) {
        return shelfService.reorderShelfBooks(shelfId, request);
    }
}
