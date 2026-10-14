package com.bookhaus.journal;

import com.bookhaus.journal.dto.CreateQuoteRequest;
import com.bookhaus.journal.dto.QuoteResponse;
import com.bookhaus.journal.dto.UpdateQuoteRequest;
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
@RequestMapping("/api/library/books/{libraryBookId}/quotes")
public class QuoteController {
    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponse createQuote(
            @PathVariable Long libraryBookId,
            @Valid @RequestBody CreateQuoteRequest request
    ) {
        return quoteService.createQuote(libraryBookId, request);
    }

    @GetMapping
    public List<QuoteResponse> getQuotes(@PathVariable Long libraryBookId) {
        return quoteService.getQuotes(libraryBookId);
    }

    @PatchMapping("/{quoteId}")
    public QuoteResponse updateQuote(
            @PathVariable Long libraryBookId,
            @PathVariable Long quoteId,
            @Valid @RequestBody UpdateQuoteRequest request
    ) {
        return quoteService.updateQuote(libraryBookId, quoteId, request);
    }

    @DeleteMapping("/{quoteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuote(@PathVariable Long libraryBookId, @PathVariable Long quoteId) {
        quoteService.deleteQuote(libraryBookId, quoteId);
    }
}
