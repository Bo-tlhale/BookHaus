import { Home } from 'lucide-react'
import { Link } from 'react-router-dom'
import EmptyState from '../components/EmptyState'

export default function NotFoundPage() { return <EmptyState title="This page wandered off the shelf." description="Let’s return to the heart of BookHaus." action={<Link to="/" className="btn-primary"><Home className="h-4 w-4" /> Go home</Link>} /> }
