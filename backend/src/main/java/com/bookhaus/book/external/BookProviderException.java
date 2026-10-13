package com.bookhaus.book.external;

public class BookProviderException extends RuntimeException {
    public BookProviderException(String message) {
        super(message);
    }

    public BookProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
