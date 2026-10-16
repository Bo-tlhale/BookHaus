CREATE INDEX idx_books_external_provider_id ON books (external_provider, external_book_id);
CREATE INDEX idx_library_books_user_status ON library_books (user_id, status);
CREATE INDEX idx_library_books_user_added_at ON library_books (user_id, added_at DESC);
CREATE INDEX idx_shelves_user_position ON shelves (user_id, position);
CREATE INDEX idx_shelf_books_shelf_position ON shelf_books (shelf_id, position);
CREATE INDEX idx_quotes_library_book_created_at ON quotes (library_book_id, created_at);
