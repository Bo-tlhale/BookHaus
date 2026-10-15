import { Link } from 'react-router-dom'
import BookCover from './BookCover'
import ProgressBar from './ProgressBar'
import StatusBadge from './StatusBadge'
import { formatAuthors } from '../utils/format'

export default function BookCard({ book, action }) {
  return <article className="book-card"><Link to={book.libraryBookId ? `/library/${book.libraryBookId}` : '#'} className="book-card__cover-link" aria-label={`Open ${book.title}`}><BookCover src={book.coverUrl} title={book.title} className="book-card__cover" /></Link><div className="book-card__body"><div className="flex items-start justify-between gap-2"><div className="min-w-0"><h2 className="line-clamp-2 font-display text-lg font-semibold leading-tight text-plum">{book.title}</h2><p className="mt-1 line-clamp-1 text-sm text-lavender-700">{formatAuthors(book.authors)}</p></div>{book.personalRating != null && <span className="shrink-0 text-sm font-bold text-gold">&#9733; {Number(book.personalRating).toFixed(1)}</span>}</div><div className="mt-3 flex flex-wrap gap-1.5">{book.genres?.slice(0, 2).map((genre) => <span className="chip" key={genre}>{genre}</span>)}{book.readingStatus && <StatusBadge status={book.readingStatus} />}</div>{book.progressPercentage != null && <div className="mt-3"><ProgressBar value={book.progressPercentage} /></div>}{action && <div className="mt-4">{action}</div>}</div></article>
}
