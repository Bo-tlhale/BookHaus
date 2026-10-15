import { useEffect, useState } from 'react'
import { ArrowRight, BookCheck, BookOpen, Feather, Library, Search, Sparkles, Target } from 'lucide-react'
import { Link } from 'react-router-dom'
import roomHero from '../assets/room/bookhaus-room-hero.jpg'
import { libraryApi } from '../api/libraryApi'
import { statsApi } from '../api/statsApi'
import BookCover from '../components/BookCover'
import ErrorMessage from '../components/ErrorMessage'
import { CardGridSkeleton } from '../components/LoadingSkeleton'
import ProgressBar from '../components/ProgressBar'
import StatCard from '../components/StatCard'
import { useAuth } from '../context/useAuth'
import { firstName, formatAuthors } from '../utils/format'

export default function HomePage() {
  const { user } = useAuth()
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  useEffect(() => { let active = true; Promise.all([statsApi.overview(), libraryApi.list()]).then(([stats, books]) => { if (active) setData({ stats, books }) }).catch(setError); return () => { active = false } }, [])
  const current = data?.books.find((book) => book.readingStatus === 'CURRENTLY_READING') ?? data?.books[0]

  return <>
    <section className="home-hero" style={{ backgroundImage: `linear-gradient(90deg, rgba(36,22,47,.88) 0%, rgba(52,32,64,.48) 42%, rgba(24,16,32,.05) 74%), url(${roomHero})` }}><div className="home-hero__copy"><p className="eyebrow !text-lavender-200">Welcome home, {firstName(user?.name)}</p><h1>Your coziest chapter starts here.</h1><p>A personal sanctuary for the stories you love, the thoughts you keep, and every brighter day ahead.</p><div className="mt-7 flex flex-wrap gap-3"><Link to={current ? `/library/${current.libraryBookId}` : '/search'} className="btn-primary"><BookOpen className="h-4 w-4" /> {current ? 'Continue reading' : 'Find your first book'}</Link><Link to="/library" className="btn-soft"><Library className="h-4 w-4" /> Enter my library</Link></div></div><p className="home-hero__note">Good books lead<br />to brighter days. <span>&hearts;</span></p></section>
    <ErrorMessage error={error} />
    {!data ? <CardGridSkeleton count={4} /> : <>
      <section className="summary-strip" aria-label="Library summary"><StatCard label="In your library" value={data.stats.totalBooksInLibrary} icon={Library} /><StatCard label="Currently reading" value={data.stats.currentlyReadingBooks} icon={BookOpen} tone="blue" /><StatCard label="Books completed" value={data.stats.completedBooks} icon={BookCheck} tone="green" /><StatCard label="Pages read" value={data.stats.totalPagesRead.toLocaleString()} icon={Sparkles} tone="wood" /></section>
      <section className="home-grid"><div className="cozy-panel home-current"><div className="section-heading"><div><p className="eyebrow">Your reading nook</p><h2>{current ? 'Continue your story' : 'Your shelves are waiting'}</h2></div><Link to="/library">See all <ArrowRight /></Link></div>{current ? <div className="continue-card"><BookCover src={current.coverUrl} title={current.title} className="h-52 w-36 rounded-2xl shadow-elevated" /><div><p className="eyebrow">Currently reading</p><h3>{current.title}</h3><p>{formatAuthors(current.authors)}</p>{current.progressPercentage != null && <div className="mt-5"><ProgressBar value={current.progressPercentage} /></div>}<Link to={`/library/${current.libraryBookId}`} className="btn-primary mt-5">Continue reading <ArrowRight className="h-4 w-4" /></Link></div></div> : <div className="empty-cozy"><Search /><p>Your first story is only a search away.</p><Link to="/search" className="btn-primary">Discover books</Link></div>}</div><div className="grid gap-4 sm:grid-cols-3 xl:grid-cols-1"><Shortcut to="/journal" icon={Feather} title="Reading journal" text="Keep the thoughts that linger." /><Shortcut to="/shelves" icon={BookOpen} title="Your shelves" text="Arrange stories your way." /><Shortcut to="/goals" icon={Target} title="Reading goals" text={data.stats.currentGoal ? `${data.stats.currentGoal.completedBooks} of ${data.stats.currentGoal.targetBooks} books complete.` : 'Set a gentle intention.'} /></div></section>
    </>}
  </>
}

function Shortcut({ to, icon: Icon, title, text }) {
  return <Link to={to} className="shortcut-card"><span><Icon /></span><div><h3>{title}</h3><p>{text}</p></div><ArrowRight /></Link>
}
