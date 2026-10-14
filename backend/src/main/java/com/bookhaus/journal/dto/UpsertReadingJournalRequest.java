package com.bookhaus.journal.dto;

public record UpsertReadingJournalRequest(
        String aboutBook,
        String feelings,
        String reflection,
        String lessonsLearned
) {
}
