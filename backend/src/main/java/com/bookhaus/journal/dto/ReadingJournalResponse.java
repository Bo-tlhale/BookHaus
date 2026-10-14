package com.bookhaus.journal.dto;

import com.bookhaus.journal.ReadingJournal;
import java.time.Instant;

public record ReadingJournalResponse(
        Long id,
        Long libraryBookId,
        String aboutBook,
        String feelings,
        String reflection,
        String lessonsLearned,
        Instant updatedAt
) {
    public static ReadingJournalResponse from(ReadingJournal journal) {
        return new ReadingJournalResponse(
                journal.getId(),
                journal.getLibraryBook().getId(),
                journal.getAboutBook(),
                journal.getFeelings(),
                journal.getReflection(),
                journal.getLessonsLearned(),
                journal.getUpdatedAt()
        );
    }
}
