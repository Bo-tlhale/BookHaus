import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import GoalsPage from './GoalsPage'
import JournalPage from './JournalPage'
import LibraryPage from './LibraryPage'
import SearchPage from './SearchPage'
import ShelvesPage from './ShelvesPage'
import { booksApi } from '../api/booksApi'
import { goalsApi } from '../api/goalsApi'
import { libraryApi } from '../api/libraryApi'
import { shelvesApi } from '../api/shelvesApi'
import { statsApi } from '../api/statsApi'

vi.mock('../api/booksApi', () => ({ booksApi: { search: vi.fn() } }))
vi.mock('../api/libraryApi', () => ({ libraryApi: { list: vi.fn(), add: vi.fn() } }))
vi.mock('../api/shelvesApi', () => ({ shelvesApi: { list: vi.fn(), create: vi.fn(), rename: vi.fn(), remove: vi.fn(), reorder: vi.fn() } }))
vi.mock('../api/goalsApi', () => ({ goalsApi: { list: vi.fn(), create: vi.fn(), update: vi.fn(), remove: vi.fn() } }))
vi.mock('../api/statsApi', () => ({ statsApi: { overview: vi.fn() } }))

const book = { libraryBookId: 1, externalId: 'OL1W', title: 'A Quiet Story', authors: ['Ada Reader'], genres: ['Fiction'], readingStatus: 'CURRENTLY_READING', progressPercentage: 35, addedAt: '2026-01-01T00:00:00Z' }
const stats = { totalBooksInLibrary: 1, completedBooks: 0, currentlyReadingBooks: 1, totalPagesRead: 120, averageRating: null, currentGoal: null }

function renderPage(component, path = '/') {
  return render(<MemoryRouter initialEntries={[path]}>{component}</MemoryRouter>)
}

describe('redesigned functional pages', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    libraryApi.list.mockResolvedValue([book])
    statsApi.overview.mockResolvedValue(stats)
    shelvesApi.list.mockResolvedValue([])
    goalsApi.list.mockResolvedValue([])
  })

  it('renders real library books in the redesigned library', async () => {
    renderPage(<LibraryPage />)
    expect(await screen.findByRole('heading', { name: 'A Quiet Story' })).toBeInTheDocument()
    expect(screen.getAllByText('Currently Reading')).not.toHaveLength(0)
  })

  it('searches through the backend and keeps the add action functional', async () => {
    booksApi.search.mockResolvedValue([{ ...book, libraryBookId: undefined, description: 'A gentle story.', pageCount: 240, publicationYear: 2024 }])
    libraryApi.add.mockResolvedValue(book)
    renderPage(<SearchPage />, '/search?q=quiet')
    expect(await screen.findByRole('heading', { name: 'A Quiet Story' })).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: /add to my library/i }))
    await waitFor(() => expect(libraryApi.add).toHaveBeenCalledOnce())
  })

  it('uses library data for the journal index', async () => {
    renderPage(<JournalPage />)
    expect(await screen.findByText('Choose a book to keep writing')).toBeInTheDocument()
    expect(screen.getByText('A Quiet Story')).toBeInTheDocument()
  })

  it('renders the real empty shelf state without fixed genre shelves', async () => {
    renderPage(<ShelvesPage />)
    expect(await screen.findByText('Create a shelf for the books that belong together.')).toBeInTheDocument()
    expect(shelvesApi.list).toHaveBeenCalledOnce()
  })

  it('loads goal and stats data together', async () => {
    goalsApi.list.mockResolvedValue([{ year: 2026, targetBooks: 12, completedBooks: 3, remainingBooks: 9, progressPercentage: 25, onPace: true }])
    renderPage(<GoalsPage />)
    expect(await screen.findByText('Reading Goals & Stats')).toBeInTheDocument()
    expect(screen.getByText('3')).toBeInTheDocument()
    expect(statsApi.overview).toHaveBeenCalledOnce()
  })
})
