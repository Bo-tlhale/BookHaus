package com.bookhaus.journal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;

public class UpdateQuoteRequest {
    private String quoteText;
    @Positive
    private Integer pageNumber;
    private String note;
    private boolean quoteTextSupplied;
    private boolean pageNumberSupplied;
    private boolean noteSupplied;

    public String quoteText() {
        return quoteText;
    }

    public void setQuoteText(String quoteText) {
        this.quoteText = quoteText;
        this.quoteTextSupplied = true;
    }

    public Integer pageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
        this.pageNumberSupplied = true;
    }

    public String note() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
        this.noteSupplied = true;
    }

    @JsonIgnore
    public boolean isQuoteTextSupplied() {
        return quoteTextSupplied;
    }

    @JsonIgnore
    public boolean isPageNumberSupplied() {
        return pageNumberSupplied;
    }

    @JsonIgnore
    public boolean isNoteSupplied() {
        return noteSupplied;
    }

    @AssertTrue(message = "Quote text must not be blank when supplied")
    @JsonIgnore
    public boolean isQuoteTextValid() {
        return !quoteTextSupplied || quoteText != null && !quoteText.isBlank();
    }
}
