package com.bookhaus.shelf;

import com.bookhaus.library.LibraryBook;
import com.bookhaus.library.LibraryBookRepository;
import com.bookhaus.shelf.dto.AddShelfBookRequest;
import com.bookhaus.shelf.dto.CreateShelfRequest;
import com.bookhaus.shelf.dto.RenameShelfRequest;
import com.bookhaus.shelf.dto.ReorderShelfBooksRequest;
import com.bookhaus.shelf.dto.ReorderShelvesRequest;
import com.bookhaus.shelf.dto.ShelfBookResponse;
import com.bookhaus.shelf.dto.ShelfResponse;
import com.bookhaus.user.User;
import com.bookhaus.user.UserService;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShelfService {
    private final UserService userService;
    private final ShelfRepository shelfRepository;
    private final ShelfBookRepository shelfBookRepository;
    private final LibraryBookRepository libraryBookRepository;

    public ShelfService(
            UserService userService,
            ShelfRepository shelfRepository,
            ShelfBookRepository shelfBookRepository,
            LibraryBookRepository libraryBookRepository
    ) {
        this.userService = userService;
        this.shelfRepository = shelfRepository;
        this.shelfBookRepository = shelfBookRepository;
        this.libraryBookRepository = libraryBookRepository;
    }

    @Transactional(readOnly = true)
    public List<ShelfResponse> getShelves() {
        User user = userService.getCurrentUserEntity();
        return shelfRepository.findAllByUserOrderByPositionAsc(user).stream()
                .map(ShelfResponse::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShelfResponse getShelf(Long shelfId) {
        User user = userService.getCurrentUserEntity();
        Shelf shelf = findOwnedShelf(shelfId, user);
        List<ShelfBookResponse> books = shelfBookRepository.findAllByShelfOrderByPositionAsc(shelf).stream()
                .map(ShelfBookResponse::from)
                .toList();
        return ShelfResponse.details(shelf, books);
    }

    @Transactional
    public ShelfResponse createShelf(CreateShelfRequest request) {
        User user = userService.getCurrentUserEntity();
        String name = normalizeName(request.name());
        if (shelfRepository.existsByUserAndNameIgnoreCase(user, name)) {
            throw duplicateShelfName();
        }

        int position = shelfRepository.findTopByUserOrderByPositionDesc(user)
                .map(shelf -> shelf.getPosition() + 1)
                .orElse(0);
        Shelf shelf = new Shelf();
        shelf.setUser(user);
        shelf.setName(name);
        shelf.setPosition(position);
        try {
            return ShelfResponse.summary(shelfRepository.saveAndFlush(shelf));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateShelfName();
        }
    }

    @Transactional
    public ShelfResponse renameShelf(Long shelfId, RenameShelfRequest request) {
        User user = userService.getCurrentUserEntity();
        Shelf shelf = findOwnedShelf(shelfId, user);
        String name = normalizeName(request.name());
        if (shelfRepository.existsByUserAndNameIgnoreCaseAndIdNot(user, name, shelfId)) {
            throw duplicateShelfName();
        }
        shelf.setName(name);
        try {
            return ShelfResponse.summary(shelfRepository.saveAndFlush(shelf));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateShelfName();
        }
    }

    @Transactional
    public void deleteShelf(Long shelfId) {
        Shelf shelf = findOwnedShelf(shelfId, userService.getCurrentUserEntity());
        shelfRepository.delete(shelf);
    }

    @Transactional
    public ShelfBookResponse addBook(Long shelfId, AddShelfBookRequest request) {
        User user = userService.getCurrentUserEntity();
        Shelf shelf = findOwnedShelf(shelfId, user);
        LibraryBook libraryBook = findOwnedLibraryBook(request.libraryBookId(), user);
        if (shelfBookRepository.existsByShelfAndLibraryBook(shelf, libraryBook)) {
            throw duplicateShelfBook();
        }

        int position = shelfBookRepository.findTopByShelfOrderByPositionDesc(shelf)
                .map(shelfBook -> shelfBook.getPosition() + 1)
                .orElse(0);
        ShelfBook shelfBook = new ShelfBook();
        shelfBook.setShelf(shelf);
        shelfBook.setLibraryBook(libraryBook);
        shelfBook.setPosition(position);
        try {
            return ShelfBookResponse.from(shelfBookRepository.saveAndFlush(shelfBook));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateShelfBook();
        }
    }

    @Transactional
    public void removeBook(Long shelfId, Long libraryBookId) {
        User user = userService.getCurrentUserEntity();
        Shelf shelf = findOwnedShelf(shelfId, user);
        LibraryBook libraryBook = findOwnedLibraryBook(libraryBookId, user);
        ShelfBook shelfBook = shelfBookRepository.findByShelfAndLibraryBook(shelf, libraryBook)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf book not found"));
        shelfBookRepository.delete(shelfBook);
    }

    @Transactional
    public List<ShelfResponse> reorderShelves(ReorderShelvesRequest request) {
        User user = userService.getCurrentUserEntity();
        List<Shelf> shelves = shelfRepository.findAllByUserOrderByPositionAsc(user);
        validateCompleteOrder(request.shelfIds(), shelves.stream().map(Shelf::getId).toList(), "shelves");
        Map<Long, Shelf> byId = shelves.stream().collect(Collectors.toMap(Shelf::getId, Function.identity()));
        for (int position = 0; position < request.shelfIds().size(); position++) {
            byId.get(request.shelfIds().get(position)).setPosition(position);
        }
        shelfRepository.saveAll(shelves);
        return request.shelfIds().stream().map(byId::get).map(ShelfResponse::summary).toList();
    }

    @Transactional
    public ShelfResponse reorderShelfBooks(Long shelfId, ReorderShelfBooksRequest request) {
        User user = userService.getCurrentUserEntity();
        Shelf shelf = findOwnedShelf(shelfId, user);
        List<ShelfBook> shelfBooks = shelfBookRepository.findAllByShelfOrderByPositionAsc(shelf);
        List<Long> currentIds = shelfBooks.stream().map(item -> item.getLibraryBook().getId()).toList();
        validateCompleteOrder(request.libraryBookIds(), currentIds, "shelf books");
        Map<Long, ShelfBook> byLibraryBookId = shelfBooks.stream().collect(Collectors.toMap(
                item -> item.getLibraryBook().getId(), Function.identity()));
        for (int position = 0; position < request.libraryBookIds().size(); position++) {
            byLibraryBookId.get(request.libraryBookIds().get(position)).setPosition(position);
        }
        shelfBookRepository.saveAll(shelfBooks);
        List<ShelfBookResponse> books = request.libraryBookIds().stream()
                .map(byLibraryBookId::get)
                .map(ShelfBookResponse::from)
                .toList();
        return ShelfResponse.details(shelf, books);
    }

    private Shelf findOwnedShelf(Long shelfId, User user) {
        return shelfRepository.findByIdAndUser(shelfId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));
    }

    private LibraryBook findOwnedLibraryBook(Long libraryBookId, User user) {
        return libraryBookRepository.findByIdAndUser(libraryBookId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Library book not found"));
    }

    private void validateCompleteOrder(List<Long> requestedIds, List<Long> currentIds, String resourceName) {
        Set<Long> requestedSet = new HashSet<>(requestedIds);
        Set<Long> currentSet = new HashSet<>(currentIds);
        if (requestedSet.size() != requestedIds.size()
                || requestedIds.size() != currentIds.size()
                || !requestedSet.equals(currentSet)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reorder request must contain every current " + resourceName + " ID exactly once"
            );
        }
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }

    private ResponseStatusException duplicateShelfName() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "A shelf with this name already exists");
    }

    private ResponseStatusException duplicateShelfBook() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "This book is already on the shelf");
    }
}
