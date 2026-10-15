import { describe, expect, it } from 'vitest'
import { filterLibrary } from './libraryFilters'

const books = [
  { libraryBookId: 1, readingStatus: 'TO_READ' },
  { libraryBookId: 2, readingStatus: 'COMPLETED' },
]

describe('filterLibrary', () => {
  it('returns every book when no status is selected', () => {
    expect(filterLibrary(books, '')).toEqual(books)
  })

  it('returns only books with the selected status', () => {
    expect(filterLibrary(books, 'COMPLETED')).toEqual([books[1]])
  })
})
