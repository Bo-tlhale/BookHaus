package com.bookhaus.book;

import com.bookhaus.book.dto.BookDetailsResponse;
import com.bookhaus.book.dto.BookSearchResult;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/search")
    public List<BookSearchResult> search(@RequestParam String q) {
        return bookService.search(q);
    }

    @GetMapping("/{id}")
    public BookDetailsResponse getById(@PathVariable Long id) {
        return bookService.getById(id);
    }
}
