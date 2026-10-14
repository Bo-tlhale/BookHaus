package com.bookhaus.journal;

import com.bookhaus.journal.dto.ReadingJournalResponse;
import com.bookhaus.journal.dto.UpsertReadingJournalRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library/books/{libraryBookId}/journal")
public class ReadingJournalController {
    private final ReadingJournalService readingJournalService;

    public ReadingJournalController(ReadingJournalService readingJournalService) {
        this.readingJournalService = readingJournalService;
    }

    @GetMapping
    public ReadingJournalResponse getJournal(@PathVariable Long libraryBookId) {
        return readingJournalService.getJournal(libraryBookId);
    }

    @PutMapping
    public ReadingJournalResponse upsertJournal(
            @PathVariable Long libraryBookId,
            @Valid @RequestBody UpsertReadingJournalRequest request
    ) {
        return readingJournalService.upsertJournal(libraryBookId, request);
    }
}
