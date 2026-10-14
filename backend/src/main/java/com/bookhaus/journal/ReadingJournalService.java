package com.bookhaus.journal;

import com.bookhaus.journal.dto.ReadingJournalResponse;
import com.bookhaus.journal.dto.UpsertReadingJournalRequest;
import com.bookhaus.library.LibraryBook;
import com.bookhaus.library.LibraryBookRepository;
import com.bookhaus.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReadingJournalService {
    private final UserService userService;
    private final LibraryBookRepository libraryBookRepository;
    private final ReadingJournalRepository readingJournalRepository;

    public ReadingJournalService(
            UserService userService,
            LibraryBookRepository libraryBookRepository,
            ReadingJournalRepository readingJournalRepository
    ) {
        this.userService = userService;
        this.libraryBookRepository = libraryBookRepository;
        this.readingJournalRepository = readingJournalRepository;
    }

    @Transactional(readOnly = true)
    public ReadingJournalResponse getJournal(Long libraryBookId) {
        LibraryBook libraryBook = findOwnedLibraryBook(libraryBookId);
        return readingJournalRepository.findByLibraryBook(libraryBook)
                .map(ReadingJournalResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reading journal not found"));
    }

    @Transactional
    public ReadingJournalResponse upsertJournal(Long libraryBookId, UpsertReadingJournalRequest request) {
        LibraryBook libraryBook = findOwnedLibraryBook(libraryBookId);
        ReadingJournal journal = readingJournalRepository.findByLibraryBook(libraryBook)
                .orElseGet(() -> {
                    ReadingJournal created = new ReadingJournal();
                    created.setLibraryBook(libraryBook);
                    return created;
                });

        journal.setAboutBook(request.aboutBook());
        journal.setFeelings(request.feelings());
        journal.setReflection(request.reflection());
        journal.setLessonsLearned(request.lessonsLearned());
        return ReadingJournalResponse.from(readingJournalRepository.saveAndFlush(journal));
    }

    private LibraryBook findOwnedLibraryBook(Long libraryBookId) {
        return libraryBookRepository.findByIdAndUser(libraryBookId, userService.getCurrentUserEntity())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Library book not found"));
    }
}
