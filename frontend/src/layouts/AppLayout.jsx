import { Outlet } from 'react-router-dom'
import BookHausHeader from '../components/BookHausHeader'

export default function AppLayout() {
  return <div className="app-shell"><div className="app-shell__backdrop" aria-hidden="true" /><BookHausHeader /><main className="app-content"><div className="page-enter mx-auto w-full max-w-[1500px]"><Outlet /></div></main></div>
}
