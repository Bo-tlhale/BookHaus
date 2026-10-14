package com.bookhaus.library;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhaus.book.Author;
import com.bookhaus.book.AuthorRepository;
import com.bookhaus.book.Book;
import com.bookhaus.book.BookRepository;
import com.bookhaus.book.Genre;
import com.bookhaus.book.GenreRepository;
import com.bookhaus.book.dto.AddBookRequest;
import com.bookhaus.user.User;
import com.bookhaus.user.UserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {
    @Mock UserService userService;
    @Mock BookRepository bookRepository;
    @Mock AuthorRepository authorRepository;
    @Mock GenreRepository genreRepository;
    @Mock LibraryBookRepository libraryBookRepository;
    private LibraryService service;
    private User currentUser;

    @BeforeEach
    void setUp() {
        service = new LibraryService(userService, bookRepository, authorRepository, genreRepository, libraryBookRepository);
        currentUser = new User();
        currentUser.setId(7L);
        currentUser.setEmail("reader@example.com");
        when(userService.getCurrentUserEntity()).thenReturn(currentUser);
    }

    @Test
    void persistsNewBookAndReusesCaseInsensitiveAuthorsAndGenres() {
        AddBookRequest request = request(ReadingStatus.CURRENTLY_READING);
        Author author = new Author();
        author.setId(2L);
        author.setName("Frank Herbert");
        Genre genre = new Genre();
        genre.setId(3L);
        genre.setName("Science Fiction");
        when(bookRepository.findByExternalProviderAndExternalBookId("OPEN_LIBRARY", "/works/OL893415W")).thenReturn(Optional.empty());
        when(bookRepository.findFirstByIsbn13("9780441172719")).thenReturn(Optional.empty());
        when(bookRepository.findFirstByTitleIgnoreCaseAndPublicationYear("Dune", 1965)).thenReturn(Optional.empty());
        when(authorRepository.findByNameIgnoreCase("Frank Herbert")).thenReturn(Optional.of(author));
        when(genreRepository.findByNameIgnoreCase("Science Fiction")).thenReturn(Optional.of(genre));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(11L);
            return book;
        });
        when(libraryBookRepository.existsByUserAndBook(any(), any())).thenReturn(false);
        when(libraryBookRepository.saveAndFlush(any(LibraryBook.class))).thenAnswer(invocation -> {
            LibraryBook libraryBook = invocation.getArgument(0);
            libraryBook.setId(12L);
            return libraryBook;
        });

        var response = service.addBook(request);

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookCaptor.capture());
        Book stored = bookCaptor.getValue();
        assertThat(stored.getExternalProvider()).isEqualTo("OPEN_LIBRARY");
        assertThat(stored.getExternalBookId()).isEqualTo("/works/OL893415W");
        assertThat(stored.getIsbn13()).isEqualTo("9780441172719");
        assertThat(stored.getAuthors()).containsExactly(author);
        assertThat(stored.getGenres()).containsExactly(genre);
        assertThat(response.readingStatus()).isEqualTo(ReadingStatus.CURRENTLY_READING);
        assertThat(response.bookId()).isEqualTo(11L);

        ArgumentCaptor<LibraryBook> libraryCaptor = ArgumentCaptor.forClass(LibraryBook.class);
        verify(libraryBookRepository).saveAndFlush(libraryCaptor.capture());
        assertThat(libraryCaptor.getValue().getUser()).isSameAs(currentUser);
    }

    @Test
    void createsMissingAuthorAndGenreAndDefaultsStatus() {
        AddBookRequest request = request(null);
        stubNewBookLookup();
        when(authorRepository.findByNameIgnoreCase("Frank Herbert")).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(genreRepository.findByNameIgnoreCase("Science Fiction")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(libraryBookRepository.existsByUserAndBook(any(), any())).thenReturn(false);
        when(libraryBookRepository.saveAndFlush(any(LibraryBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.addBook(request).readingStatus()).isEqualTo(ReadingStatus.TO_READ);
        verify(authorRepository).save(any(Author.class));
        verify(genreRepository).save(any(Genre.class));
    }

    @Test
    void reusesExistingBookByExternalIdentityAndRejectsUserDuplicate() {
        Book existing = new Book();
        existing.setId(5L);
        existing.setTitle("Dune");
        when(bookRepository.findByExternalProviderAndExternalBookId("OPEN_LIBRARY", "/works/OL893415W"))
                .thenReturn(Optional.of(existing));
        when(libraryBookRepository.existsByUserAndBook(currentUser, existing)).thenReturn(true);

        assertThatThrownBy(() -> service.addBook(request(null)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verify(bookRepository, never()).save(any());
        verify(libraryBookRepository, never()).saveAndFlush(any());
    }

    @Test
    void translatesDatabaseDuplicateConstraintToConflict() {
        Book existing = new Book();
        existing.setId(5L);
        existing.setTitle("Dune");
        when(bookRepository.findByExternalProviderAndExternalBookId(any(), any())).thenReturn(Optional.of(existing));
        when(libraryBookRepository.existsByUserAndBook(currentUser, existing)).thenReturn(false);
        when(libraryBookRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertThatThrownBy(() -> service.addBook(request(null)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    private void stubNewBookLookup() {
        when(bookRepository.findByExternalProviderAndExternalBookId(any(), any())).thenReturn(Optional.empty());
        when(bookRepository.findFirstByIsbn13(any())).thenReturn(Optional.empty());
        when(bookRepository.findFirstByTitleIgnoreCaseAndPublicationYear(any(), any())).thenReturn(Optional.empty());
    }

    private AddBookRequest request(ReadingStatus status) {
        return new AddBookRequest(
                "/works/OL893415W", " Dune ", List.of("Frank Herbert"), "Epic science fiction",
                "978-0-441-17271-9", "https://covers.example/dune.jpg", List.of("Science Fiction"),
                "Ace", 1965, 412, status
        );
    }
}
