import { useEffect, useMemo, useState } from 'react'
import { BookCheck, BookOpen, Heart, Library, Search } from 'lucide-react'
import { Link } from 'react-router-dom'
import { libraryApi } from '../api/libraryApi'
import BookCard from '../components/BookCard'
import EmptyState from '../components/EmptyState'
import ErrorMessage from '../components/ErrorMessage'
import { CardGridSkeleton } from '../components/LoadingSkeleton'
import PageHero from '../components/PageHero'
import StatCard from '../components/StatCard'
import { READING_STATUSES, STATUS_LABELS } from '../utils/status'

export default function LibraryPage() {
  const [books, setBooks] = useState([]); const [loading, setLoading] = useState(true); const [error, setError] = useState(null)
  const [status, setStatus] = useState(''); const [query, setQuery] = useState(''); const [sort, setSort] = useState('recent')
  useEffect(() => { let active = true; libraryApi.list().then((items) => { if (active) setBooks(items) }).catch(setError).finally(() => { if (active) setLoading(false) }); return () => { active = false } }, [])
  const counts = useMemo(() => Object.fromEntries(READING_STATUSES.map((item) => [item, books.filter((book) => book.readingStatus === item).length])), [books])
  const filtered = useMemo(() => books.filter((book) => (!status || book.readingStatus === status) && (!query || `${book.title} ${book.authors?.join(' ')} ${book.genres?.join(' ')}`.toLowerCase().includes(query.toLowerCase()))).sort((a, b) => sort === 'title' ? a.title.localeCompare(b.title) : new Date(b.addedAt) - new Date(a.addedAt)), [books, status, query, sort])
  return <><PageHero compact eyebrow="Your collection, your cozy corner" title="My Library" description="Keep track of what you are reading, discover old favorites, and revisit the stories that mean the most." note="A home for every book and every part of you." /><div className="library-layout"><aside className="library-sidebar cozy-panel"><h2><Library /> My Library</h2><SidebarFilter label="All Books" count={books.length} active={!status} onClick={() => setStatus('')} icon={Library} />{READING_STATUSES.map((item) => <SidebarFilter key={item} label={STATUS_LABELS[item]} count={counts[item]} active={status === item} onClick={() => setStatus(item)} icon={item === 'COMPLETED' ? BookCheck : item === 'WISHLIST' ? Heart : BookOpen} />)}<div className="library-sidebar__note">Every shelf tells a little part of your story.</div></aside><div className="min-w-0"><section className="summary-strip summary-strip--library"><StatCard label="Total books" value={books.length} icon={Library} /><StatCard label="Currently reading" value={counts.CURRENTLY_READING || 0} icon={BookOpen} tone="blue" /><StatCard label="Completed" value={counts.COMPLETED || 0} icon={BookCheck} tone="green" /><StatCard label="On wishlist" value={counts.WISHLIST || 0} icon={Heart} tone="wood" /></section><div className="library-toolbar"><div className="search-field"><Search /><label className="sr-only" htmlFor="library-search">Search your library</label><input id="library-search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search your library..." /></div><select value={sort} onChange={(event) => setSort(event.target.value)} aria-label="Sort library"><option value="recent">Recently added</option><option value="title">Title A-Z</option></select><Link to="/search" className="btn-primary"><Search className="h-4 w-4" /> Find books</Link></div><ErrorMessage error={error} />{loading ? <CardGridSkeleton /> : filtered.length ? <div className="book-grid">{filtered.map((book) => <BookCard key={book.libraryBookId} book={book} />)}</div> : <EmptyState title={status ? `No ${STATUS_LABELS[status].toLowerCase()} books yet` : 'Your shelves are waiting for their first story.'} description="Find something wonderful and add it to your library." action={<Link to="/search" className="btn-primary">Explore books</Link>} />}</div></div></>
}

function SidebarFilter({ label, count, active, onClick, icon: Icon }) {
  return <button type="button" className={`library-sidebar__item ${active ? 'is-active' : ''}`} onClick={onClick}><Icon /><span>{label}</span><b>{count}</b></button>
}
