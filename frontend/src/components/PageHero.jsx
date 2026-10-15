export default function PageHero({ eyebrow, title, description, note, image, compact = false, children }) {
  const style = image ? { backgroundImage: `linear-gradient(90deg, rgba(255,250,242,.97) 0%, rgba(255,250,242,.83) 40%, rgba(255,250,242,.08) 72%), url(${image})` } : undefined
  return <header className={`page-hero ${compact ? 'page-hero--compact' : ''} ${image ? 'page-hero--image' : ''}`} style={style}><div className="relative z-10 max-w-2xl">{eyebrow && <p className="eyebrow">{eyebrow}</p>}<h1>{title}<span className="text-lavender-400"> &hearts;</span></h1>{description && <p>{description}</p>}{children}</div>{note && <p className="decorative-note">{note}</p>}</header>
}
