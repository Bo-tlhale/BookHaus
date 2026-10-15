export default function PageHeader({ eyebrow, title, description, actions }) {
  return <header className="page-hero page-hero--compact flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between"><div>{eyebrow && <p className="eyebrow">{eyebrow}</p>}<h1>{title}<span className="text-lavender-400"> &hearts;</span></h1>{description && <p>{description}</p>}</div>{actions && <div className="relative z-10 flex shrink-0 flex-wrap gap-2">{actions}</div>}</header>
}
