import { useEffect, useState } from 'react'
import { BookCheck, BookOpen, Feather, Quote } from 'lucide-react'
import { Link } from 'react-router-dom'
import roomHero from '../assets/room/bookhaus-room-hero.jpg'
import { libraryApi } from '../api/libraryApi'
import BookCover from '../components/BookCover'
import EmptyState from '../components/EmptyState'
import ErrorMessage from '../components/ErrorMessage'
import { CardGridSkeleton } from '../components/LoadingSkeleton'
import PageHero from '../components/PageHero'
import { formatAuthors } from '../utils/format'

export default function JournalPage() {
  const [books, setBooks] = useState([]); const [error, setError] = useState(null); const [loading, setLoading] = useState(true)
  useEffect(() => { libraryApi.list().then(setBooks).catch(setError).finally(() => setLoading(false)) }, [])
  const entries = [...books.filter((book) => book.readingStatus === 'CURRENTLY_READING'), ...books.filter((book) => book.readingStatus !== 'CURRENTLY_READING')]
  return <><PageHero image={roomHero} eyebrow="Your private pages" title="My Reading Journal" description="Return to the books that moved you and keep the thoughts, feelings, lessons, and lines you want to remember." /><ErrorMessage error={error} />{loading ? <CardGridSkeleton /> : entries.length ? <div className="journal-layout"><aside className="cozy-panel journal-sidebar"><h2><Feather /> My Journal</h2><div><span><BookOpen /> All entries</span><b>{entries.length}</b></div><div><span><BookOpen /> Currently reading</span><b>{books.filter((book) => book.readingStatus === 'CURRENTLY_READING').length}</b></div><div><span><BookCheck /> Completed</span><b>{books.filter((book) => book.readingStatus === 'COMPLETED').length}</b></div><div><span><Quote /> Quotes live inside each book</span></div><p className="decorative-note !static !mt-auto !rotate-0">Different books live in different parts of us.</p></aside><section className="cozy-panel p-5 sm:p-7"><div className="section-heading"><div><p className="eyebrow">Recent entries</p><h2>Choose a book to keep writing</h2></div></div><div className="journal-book-list">{entries.map((book) => <Link to={`/library/${book.libraryBookId}`} key={book.libraryBookId}><BookCover src={book.coverUrl} title={book.title} compact className="h-20 w-14 rounded-lg" /><div><h3>{book.title}</h3><p>{formatAuthors(book.authors)}</p><span>{book.readingStatus === 'CURRENTLY_READING' ? 'Continue journal' : 'Open journal'}</span></div></Link>)}</div></section></div> : <EmptyState icon={Feather} title="Your journal is waiting for its first story." description="Add a book to your library, then open it to begin reflecting." action={<Link to="/search" className="btn-primary">Discover books</Link>} />}</>
}
