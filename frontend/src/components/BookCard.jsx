import { Link } from 'react-router-dom'
import BookCover from './BookCover'
import ProgressBar from './ProgressBar'
import StatusBadge from './StatusBadge'
import { formatAuthors } from '../utils/format'

export default function BookCard({ book, action }) {
  const content = <><BookCover src={book.coverUrl} title={book.title} className="h-44 w-28 shrink-0 rounded-xl shadow-md" /><div className="min-w-0 flex-1"><StatusBadge status={book.readingStatus} /><h2 className="mt-3 line-clamp-2 font-display text-xl font-semibold leading-tight text-wood-900">{book.title}</h2><p className="mt-1 line-clamp-2 text-sm text-stone-500">{formatAuthors(book.authors)}</p>{book.progressPercentage !== null && book.progressPercentage !== undefined && <div className="mt-4"><ProgressBar value={book.progressPercentage} /></div>}{book.personalRating !== null && book.personalRating !== undefined && <p className="mt-3 text-sm font-semibold text-amber-600">★ {Number(book.personalRating).toFixed(1)}</p>}{action && <div className="mt-4" onClick={(e) => e.preventDefault()}>{action}</div>}</div></>
  const classes = 'panel flex h-full gap-4 p-4 transition hover:-translate-y-0.5 hover:shadow-soft'
  return book.libraryBookId ? <Link to={`/library/${book.libraryBookId}`} className={classes}>{content}</Link> : <article className={classes}>{content}</article>
}
