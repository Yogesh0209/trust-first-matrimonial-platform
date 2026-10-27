import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import Logo from './Logo'
import { useAuth } from '../context/AuthContext'
import { displayName } from '../utils/roles'

export default function SidebarLayout({ badge, items }) {
  const { email, logout } = useAuth()
  const navigate = useNavigate()
  const name = displayName(email)

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const sideClass = ({ isActive }) =>
    `rounded-lg px-3 py-2 text-sm ${
      isActive ? 'bg-brand-soft font-semibold text-brand' : 'text-muted hover:bg-surface'
    }`

  const mobileClass = ({ isActive }) =>
    `whitespace-nowrap rounded-full px-3 py-1 text-xs ${
      isActive ? 'bg-brand font-semibold text-white' : 'bg-sand text-muted'
    }`

  return (
    <div className="min-h-screen">
      <header className="flex h-16 items-center justify-between border-b border-line bg-white px-4">
        <div className="flex items-center gap-3">
          <Logo />
          <span className="rounded-full bg-brand-soft px-2 py-0.5 text-xs font-semibold text-brand">
            {badge}
          </span>
        </div>
        <div className="flex items-center gap-3 text-sm">
          <span className="hidden sm:inline">{name}</span>
          <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-soft text-sm font-semibold text-brand">
            {name.charAt(0)}
          </span>
        </div>
      </header>

      <nav className="flex gap-2 overflow-x-auto border-b border-line bg-white px-4 py-2 md:hidden">
        {items.map((i) => (
          <NavLink key={i.to} to={i.to} end={i.end} className={mobileClass}>
            {i.label}
          </NavLink>
        ))}
      </nav>

      <div className="flex">
        <aside className="hidden min-h-[calc(100vh-4rem)] w-56 shrink-0 flex-col justify-between border-r border-line bg-white p-3 md:flex">
          <nav className="flex flex-col gap-1">
            {items.map((i) => (
              <NavLink key={i.to} to={i.to} end={i.end} className={sideClass}>
                {i.label}
              </NavLink>
            ))}
          </nav>
          <button onClick={handleLogout} className="px-3 py-2 text-left text-xs font-semibold text-brand">
            Log out
          </button>
        </aside>

        <main className="min-w-0 flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}