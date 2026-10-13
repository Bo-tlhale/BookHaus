package com.bookhaus.library;

import com.bookhaus.book.Author;
import com.bookhaus.book.AuthorRepository;
import com.bookhaus.book.Book;
import com.bookhaus.book.BookRepository;
import com.bookhaus.book.Genre;
import com.bookhaus.book.GenreRepository;
import com.bookhaus.book.dto.AddBookRequest;
import com.bookhaus.book.external.OpenLibraryClient;
import com.bookhaus.library.dto.LibraryBookResponse;
import com.bookhaus.user.User;
import com.bookhaus.user.UserService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LibraryService {
    static final String OPEN_LIBRARY = "OPEN_LIBRARY";

    private final UserService userService;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final LibraryBookRepository libraryBookRepository;

    public LibraryService(
            UserService userService,
            BookRepository bookRepository,
            AuthorRepository authorRepository,
            GenreRepository genreRepository,
            LibraryBookRepository libraryBookRepository
    ) {
        this.userService = userService;
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.libraryBookRepository = libraryBookRepository;
    }

    @Transactional
    public LibraryBookResponse addBook(AddBookRequest request) {
        User user = userService.getCurrentUserEntity();
        String externalId = OpenLibraryClient.normalizeWorkId(request.externalId());
        if (externalId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected Open Library work cannot be resolved");
        }

        String isbn = normalizeRequestedIsbn(request.isbn());
        Book book = findExistingBook(externalId, isbn, request)
                .orElseGet(() -> createBook(request, externalId, isbn));

        if (libraryBookRepository.existsByUserAndBook(user, book)) {
            throw duplicateBook();
        }

        LibraryBook libraryBook = new LibraryBook();
        libraryBook.setUser(user);
        libraryBook.setBook(book);
        libraryBook.setStatus(request.status() == null ? ReadingStatus.TO_READ : request.status());
        libraryBook.setCurrentPage(0);

        try {
            return LibraryBookResponse.from(libraryBookRepository.saveAndFlush(libraryBook));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateBook();
        }
    }

    private Optional<Book> findExistingBook(String externalId, String isbn, AddBookRequest request) {
        Optional<Book> existing = bookRepository.findByExternalProviderAndExternalBookId(OPEN_LIBRARY, externalId);
        if (existing.isPresent()) {
            return existing;
        }
        if (isbn != null) {
            existing = isbn.length() == 13
                    ? bookRepository.findFirstByIsbn13(isbn)
                    : bookRepository.findFirstByIsbn10(isbn);
            if (existing.isPresent()) {
                return existing;
            }
        }
        if (request.publicationYear() != null) {
            return bookRepository.findFirstByTitleIgnoreCaseAndPublicationYear(clean(request.title()), request.publicationYear());
        }
        return Optional.empty();
    }

    private Book createBook(AddBookRequest request, String externalId, String isbn) {
        Book book = new Book();
        book.setTitle(clean(request.title()));
        book.setDescription(clean(request.description()));
        if (isbn != null && isbn.length() == 13) {
            book.setIsbn13(isbn);
        } else if (isbn != null) {
            book.setIsbn10(isbn);
        }
        book.setCoverUrl(clean(request.coverUrl()));
        book.setPublisher(clean(request.publisher()));
        book.setPublicationYear(request.publicationYear());
        book.setPageCount(request.pageCount());
        book.setExternalProvider(OPEN_LIBRARY);
        book.setExternalBookId(externalId);
        resolveAuthors(request.authors()).forEach(book.getAuthors()::add);
        resolveGenres(request.genres()).forEach(book.getGenres()::add);
        return bookRepository.save(book);
    }

    private List<Author> resolveAuthors(List<String> names) {
        return distinctClean(names, 20).stream().map(name -> authorRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Author author = new Author();
                    author.setName(name);
                    return authorRepository.save(author);
                })).toList();
    }

    private List<Genre> resolveGenres(List<String> names) {
        return distinctClean(names, 10).stream().map(name -> genreRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Genre genre = new Genre();
                    genre.setName(name);
                    return genreRepository.save(genre);
                })).toList();
    }

    private String normalizeRequestedIsbn(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = OpenLibraryClient.normalizeIsbn(value);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ISBN must be a valid ISBN-10 or ISBN-13 format");
        }
        return normalized;
    }

    private List<String> distinctClean(List<String> values, int limit) {
        if (values == null) {
            return List.of();
        }
        Map<String, String> unique = new LinkedHashMap<>();
        values.stream().filter(Objects::nonNull).map(this::clean).filter(Objects::nonNull).forEach(value -> {
            if (unique.size() < limit) {
                unique.putIfAbsent(value.toLowerCase(Locale.ROOT), value);
            }
        });
        return List.copyOf(unique.values());
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim().replaceAll("\\s+", " ");
        return cleaned.isEmpty() ? null : cleaned;
    }

    private ResponseStatusException duplicateBook() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "This book is already in your library");
    }
}
