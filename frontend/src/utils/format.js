export const formatAuthors = (authors) => authors?.length ? authors.join(', ') : 'Unknown author'
export const formatDate = (value) => value ? new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(`${value}T00:00:00`)) : '—'
export const firstName = (name) => name?.trim().split(/\s+/)[0] || 'Reader'
export const pluralize = (count, singular, plural = `${singular}s`) => `${count} ${count === 1 ? singular : plural}`
