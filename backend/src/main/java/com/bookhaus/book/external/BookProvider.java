package com.bookhaus.book.external;

import com.bookhaus.book.dto.BookSearchResult;
import java.util.List;

public interface BookProvider {
    List<BookSearchResult> search(String query);
}
