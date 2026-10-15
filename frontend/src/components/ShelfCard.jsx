import { ArrowDown, ArrowUp, Edit3, GripVertical, MoreHorizontal, Trash2 } from 'lucide-react'
import { Link } from 'react-router-dom'

const spineColors = ['bg-lavender-500', 'bg-skybook-400', 'bg-wood-300', 'bg-leaf-500', 'bg-pink-300']

export default function ShelfCard({ shelf, index, total, onMove, onEdit, onDelete }) {
  return <article className="shelf-card"><div className="shelf-card__heading"><GripVertical aria-hidden="true" /><Link to={`/shelves/${shelf.shelfId}`}><h2>{shelf.name}</h2><p>{shelf.bookCount} {shelf.bookCount === 1 ? 'book' : 'books'}</p></Link><MoreHorizontal aria-hidden="true" /></div><Link to={`/shelves/${shelf.shelfId}`} className="mini-shelf" aria-label={`Open ${shelf.name}`}><span className="mini-shelf__plant">&#9752;</span>{Array.from({ length: Math.min(8, Math.max(3, shelf.bookCount || 0)) }, (_, bookIndex) => <i key={bookIndex} className={spineColors[bookIndex % spineColors.length]} style={{ height: `${46 + (bookIndex % 4) * 9}px` }} />)}<b /></Link><div className="shelf-card__actions"><button type="button" onClick={() => onMove(index, -1)} disabled={index === 0} aria-label={`Move ${shelf.name} up`}><ArrowUp /></button><button type="button" onClick={() => onMove(index, 1)} disabled={index === total - 1} aria-label={`Move ${shelf.name} down`}><ArrowDown /></button><button type="button" onClick={() => onEdit(shelf)} aria-label={`Rename ${shelf.name}`}><Edit3 /></button><button type="button" onClick={() => onDelete(shelf)} aria-label={`Delete ${shelf.name}`}><Trash2 /></button></div></article>
}
