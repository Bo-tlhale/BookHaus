export function filterLibrary(books, status) {
  return status ? books.filter((book) => book.readingStatus === status) : books
}
