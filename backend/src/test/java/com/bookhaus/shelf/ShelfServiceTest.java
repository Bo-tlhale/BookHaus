package com.bookhaus.shelf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhaus.book.Book;
import com.bookhaus.library.LibraryBook;
import com.bookhaus.library.LibraryBookRepository;
import com.bookhaus.library.ReadingStatus;
import com.bookhaus.shelf.dto.AddShelfBookRequest;
import com.bookhaus.shelf.dto.CreateShelfRequest;
import com.bookhaus.shelf.dto.RenameShelfRequest;
import com.bookhaus.shelf.dto.ReorderShelfBooksRequest;
import com.bookhaus.shelf.dto.ReorderShelvesRequest;
import com.bookhaus.user.User;
import com.bookhaus.user.UserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ShelfServiceTest {
    @Mock UserService userService;
    @Mock ShelfRepository shelfRepository;
    @Mock ShelfBookRepository shelfBookRepository;
    @Mock LibraryBookRepository libraryBookRepository;
    private ShelfService service;
    private User currentUser;

    @BeforeEach
    void setUp() {
        service = new ShelfService(userService, shelfRepository, shelfBookRepository, libraryBookRepository);
        currentUser = new User();
        currentUser.setId(7L);
        currentUser.setEmail("reader@example.com");
        when(userService.getCurrentUserEntity()).thenReturn(currentUser);
    }

    @Test
    void listsCurrentUsersShelvesInRepositoryOrder() {
        when(shelfRepository.findAllByUserOrderByPositionAsc(currentUser))
                .thenReturn(List.of(shelf(1L, "First", 0), shelf(2L, "Second", 1)));

        assertThat(service.getShelves()).extracting(response -> response.shelfId())
                .containsExactly(1L, 2L);
    }

    @Test
    void createsNormalizedShelfAtEnd() {
        when(shelfRepository.existsByUserAndNameIgnoreCase(currentUser, "Science Fiction")).thenReturn(false);
        when(shelfRepository.findTopByUserOrderByPositionDesc(currentUser))
                .thenReturn(Optional.of(shelf(1L, "Existing", 2)));
        when(shelfRepository.saveAndFlush(any(Shelf.class))).thenAnswer(invocation -> {
            Shelf shelf = invocation.getArgument(0);
            shelf.setId(3L);
            return shelf;
        });

        var response = service.createShelf(new CreateShelfRequest("  Science   Fiction "));

        assertThat(response.name()).isEqualTo("Science Fiction");
        assertThat(response.position()).isEqualTo(3);
        verify(shelfRepository).saveAndFlush(any(Shelf.class));
    }

    @Test
    void rejectsDuplicateShelfNameCaseInsensitively() {
        when(shelfRepository.existsByUserAndNameIgnoreCase(currentUser, "Favorites")).thenReturn(true);

        assertConflict(() -> service.createShelf(new CreateShelfRequest(" Favorites ")));
        verify(shelfRepository, never()).saveAndFlush(any());
    }

    @Test
    void renamesOwnedShelfWithNormalization() {
        Shelf shelf = shelf(1L, "Old Name", 0);
        when(shelfRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(shelf));
        when(shelfRepository.existsByUserAndNameIgnoreCaseAndIdNot(currentUser, "New Name", 1L)).thenReturn(false);
        when(shelfRepository.saveAndFlush(shelf)).thenReturn(shelf);

        assertThat(service.renameShelf(1L, new RenameShelfRequest(" New   Name ")).name()).isEqualTo("New Name");
    }

    @Test
    void cannotAccessAnotherUsersShelf() {
        when(shelfRepository.findByIdAndUser(99L, currentUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getShelf(99L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deletesShelfWithoutDeletingLibraryBooks() {
        Shelf shelf = shelf(1L, "Favorites", 0);
        when(shelfRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(shelf));

        service.deleteShelf(1L);

        verify(shelfRepository).delete(shelf);
        verify(libraryBookRepository, never()).delete(any());
    }

    @Test
    void addsOwnedLibraryBookAtEndOfOwnedShelf() {
        Shelf shelf = shelf(1L, "Favorites", 0);
        LibraryBook libraryBook = libraryBook(10L);
        ShelfBook last = shelfBook(1L, shelf, libraryBook(9L), 3);
        when(shelfRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(shelf));
        when(libraryBookRepository.findByIdAndUser(10L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(shelfBookRepository.existsByShelfAndLibraryBook(shelf, libraryBook)).thenReturn(false);
        when(shelfBookRepository.findTopByShelfOrderByPositionDesc(shelf)).thenReturn(Optional.of(last));
        when(shelfBookRepository.saveAndFlush(any(ShelfBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.addBook(1L, new AddShelfBookRequest(10L));

        assertThat(response.libraryBookId()).isEqualTo(10L);
        assertThat(response.position()).isEqualTo(4);
    }

    @Test
    void cannotAddAnotherUsersLibraryBook() {
        Shelf shelf = shelf(1L, "Favorites", 0);
        when(shelfRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(shelf));
        when(libraryBookRepository.findByIdAndUser(99L, currentUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addBook(1L, new AddShelfBookRequest(99L)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(shelfBookRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsDuplicateShelfMembership() {
        Shelf shelf = shelf(1L, "Favorites", 0);
        LibraryBook libraryBook = libraryBook(10L);
        when(shelfRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(shelf));
        when(libraryBookRepository.findByIdAndUser(10L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(shelfBookRepository.existsByShelfAndLibraryBook(shelf, libraryBook)).thenReturn(true);

        assertConflict(() -> service.addBook(1L, new AddShelfBookRequest(10L)));
        verify(shelfBookRepository, never()).saveAndFlush(any());
    }

    @Test
    void removesOnlyShelfMembership() {
        Shelf shelf = shelf(1L, "Favorites", 0);
        LibraryBook libraryBook = libraryBook(10L);
        ShelfBook membership = shelfBook(20L, shelf, libraryBook, 0);
        when(shelfRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(shelf));
        when(libraryBookRepository.findByIdAndUser(10L, currentUser)).thenReturn(Optional.of(libraryBook));
        when(shelfBookRepository.findByShelfAndLibraryBook(shelf, libraryBook)).thenReturn(Optional.of(membership));

        service.removeBook(1L, 10L);

        verify(shelfBookRepository).delete(membership);
        verify(libraryBookRepository, never()).delete(any());
    }

    @Test
    void reordersAllShelvesUsingZeroBasedPositions() {
        Shelf first = shelf(1L, "First", 0);
        Shelf second = shelf(2L, "Second", 1);
        Shelf third = shelf(3L, "Third", 2);
        when(shelfRepository.findAllByUserOrderByPositionAsc(currentUser)).thenReturn(List.of(first, second, third));

        var response = service.reorderShelves(new ReorderShelvesRequest(List.of(3L, 1L, 2L)));

        assertThat(response).extracting(item -> item.shelfId()).containsExactly(3L, 1L, 2L);
        assertThat(third.getPosition()).isZero();
        assertThat(first.getPosition()).isEqualTo(1);
        assertThat(second.getPosition()).isEqualTo(2);
        verify(shelfRepository).saveAll(List.of(first, second, third));
    }

    @Test
    void rejectsIncompleteOrDuplicateShelfReorder() {
        when(shelfRepository.findAllByUserOrderByPositionAsc(currentUser))
                .thenReturn(List.of(shelf(1L, "First", 0), shelf(2L, "Second", 1)));

        assertBadRequest(() -> service.reorderShelves(new ReorderShelvesRequest(List.of(1L, 1L))));
        verify(shelfRepository, never()).saveAll(any());
    }

    @Test
    void reordersEveryBookWithinOwnedShelf() {
        Shelf shelf = shelf(1L, "Favorites", 0);
        ShelfBook first = shelfBook(20L, shelf, libraryBook(10L), 0);
        ShelfBook second = shelfBook(21L, shelf, libraryBook(11L), 1);
        when(shelfRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(shelf));
        when(shelfBookRepository.findAllByShelfOrderByPositionAsc(shelf)).thenReturn(List.of(first, second));

        var response = service.reorderShelfBooks(1L, new ReorderShelfBooksRequest(List.of(11L, 10L)));

        assertThat(response.books()).extracting(item -> item.libraryBookId()).containsExactly(11L, 10L);
        assertThat(second.getPosition()).isZero();
        assertThat(first.getPosition()).isEqualTo(1);
        verify(shelfBookRepository).saveAll(List.of(first, second));
    }

    @Test
    void rejectsShelfBookReorderWithMissingOrForeignIds() {
        Shelf shelf = shelf(1L, "Favorites", 0);
        ShelfBook first = shelfBook(20L, shelf, libraryBook(10L), 0);
        when(shelfRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(shelf));
        when(shelfBookRepository.findAllByShelfOrderByPositionAsc(shelf)).thenReturn(List.of(first));

        assertBadRequest(() -> service.reorderShelfBooks(1L, new ReorderShelfBooksRequest(List.of(99L))));
        verify(shelfBookRepository, never()).saveAll(any());
    }

    private Shelf shelf(Long id, String name, int position) {
        Shelf shelf = new Shelf();
        shelf.setId(id);
        shelf.setUser(currentUser);
        shelf.setName(name);
        shelf.setPosition(position);
        return shelf;
    }

    private LibraryBook libraryBook(Long id) {
        Book book = new Book();
        book.setId(100L + id);
        book.setTitle("Book " + id);
        LibraryBook libraryBook = new LibraryBook();
        libraryBook.setId(id);
        libraryBook.setUser(currentUser);
        libraryBook.setBook(book);
        libraryBook.setStatus(ReadingStatus.TO_READ);
        libraryBook.setCurrentPage(0);
        return libraryBook;
    }

    private ShelfBook shelfBook(Long id, Shelf shelf, LibraryBook libraryBook, int position) {
        ShelfBook shelfBook = new ShelfBook();
        shelfBook.setId(id);
        shelfBook.setShelf(shelf);
        shelfBook.setLibraryBook(libraryBook);
        shelfBook.setPosition(position);
        return shelfBook;
    }

    private void assertConflict(Runnable action) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    private void assertBadRequest(Runnable action) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
