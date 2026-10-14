package com.bookhaus.library;

import com.bookhaus.book.dto.AddBookRequest;
import com.bookhaus.library.dto.LibraryBookResponse;
import com.bookhaus.library.dto.UpdateProgressRequest;
import com.bookhaus.library.dto.UpdateRatingRequest;
import com.bookhaus.library.dto.UpdateReadingStatusRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library/books")
public class LibraryController {
    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryBookResponse addBook(@Valid @RequestBody AddBookRequest request) {
        return libraryService.addBook(request);
    }

    @GetMapping
    public List<LibraryBookResponse> getLibrary(@RequestParam(required = false) ReadingStatus status) {
        return libraryService.getLibrary(status);
    }

    @GetMapping("/{libraryBookId}")
    public LibraryBookResponse getLibraryBook(@PathVariable Long libraryBookId) {
        return libraryService.getLibraryBook(libraryBookId);
    }

    @PatchMapping("/{libraryBookId}/status")
    public LibraryBookResponse updateStatus(
            @PathVariable Long libraryBookId,
            @Valid @RequestBody UpdateReadingStatusRequest request
    ) {
        return libraryService.updateStatus(libraryBookId, request);
    }

    @PatchMapping("/{libraryBookId}/progress")
    public LibraryBookResponse updateProgress(
            @PathVariable Long libraryBookId,
            @Valid @RequestBody UpdateProgressRequest request
    ) {
        return libraryService.updateProgress(libraryBookId, request);
    }

    @PatchMapping("/{libraryBookId}/rating")
    public LibraryBookResponse updateRating(
            @PathVariable Long libraryBookId,
            @Valid @RequestBody UpdateRatingRequest request
    ) {
        return libraryService.updateRating(libraryBookId, request);
    }

    @DeleteMapping("/{libraryBookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBook(@PathVariable Long libraryBookId) {
        libraryService.removeBook(libraryBookId);
    }
}
