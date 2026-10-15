import { useEffect, useState } from 'react'
import { Search } from 'lucide-react'
import { Link } from 'react-router-dom'
import { libraryApi } from '../api/libraryApi'
import BookCard from '../components/BookCard'
import EmptyState from '../components/EmptyState'
import ErrorMessage from '../components/ErrorMessage'
import { CardGridSkeleton } from '../components/LoadingSkeleton'
import PageHeader from '../components/PageHeader'
import { READING_STATUSES, STATUS_LABELS } from '../utils/status'

export default function LibraryPage() {
  const [status, setStatus] = useState(''); const [books, setBooks] = useState([]); const [loading, setLoading] = useState(true); const [error, setError] = useState(null)
  useEffect(() => { let active = true; setLoading(true); setError(null); libraryApi.list(status || undefined).then((items) => { if (active) setBooks(items) }).catch(setError).finally(() => { if (active) setLoading(false) }); return () => { active = false } }, [status])
  return <><PageHeader eyebrow="Your collection" title="My Library" description="Every book you’ve saved, read, paused, or hope to meet someday." actions={<Link to="/search" className="btn-primary"><Search className="h-4 w-4" /> Find books</Link>} /><div className="mb-6 flex gap-2 overflow-x-auto pb-2" aria-label="Filter library by reading status"><button type="button" onClick={() => setStatus('')} className={!status ? 'btn-primary whitespace-nowrap' : 'btn-secondary whitespace-nowrap'}>All</button>{READING_STATUSES.map((item) => <button type="button" key={item} onClick={() => setStatus(item)} className={status === item ? 'btn-primary whitespace-nowrap' : 'btn-secondary whitespace-nowrap'}>{STATUS_LABELS[item]}</button>)}</div><ErrorMessage error={error} />{loading ? <CardGridSkeleton /> : books.length ? <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">{books.map((book) => <BookCard key={book.libraryBookId} book={book} />)}</div> : <EmptyState title={status ? `No ${STATUS_LABELS[status].toLowerCase()} books yet` : 'Your shelves are waiting for their first story.'} description="Find something wonderful and add it to your library." action={<Link to="/search" className="btn-primary">Explore books</Link>} />}</>
}
