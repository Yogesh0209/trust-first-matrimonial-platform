import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import Logo from './Logo'
import { useEffect } from 'react'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'
import { displayName } from '../utils/roles'

const links = [
    { to: '/app/search', label: 'Search' },
    { to: '/app/interests', label: 'Interests' },
    { to: '/app/plans', label: 'Plans' },
    { to: '/app/verification', label: 'Verification' },
]

export default function UserLayout() {
    const { email, logout } = useAuth()
    const navigate = useNavigate()
    const name = displayName(email)
    useEffect(() => {
        api.get('/api/profiles/me').catch((err) => {
            if (err.response?.status === 404) navigate('/app/profile', { replace: true })
        })
    }, [navigate])

    const handleLogout = () => {
        logout()
        navigate('/login', { replace: true })
    }

    const desktopClass = ({ isActive }) =>
        `flex items-center border-b-2 text-sm ${isActive
            ? 'border-brand font-semibold text-brand'
            : 'border-transparent text-muted hover:text-ink'
        }`

    const mobileClass = ({ isActive }) =>
        `whitespace-nowrap rounded-full px-3 py-1 text-xs ${isActive ? 'bg-brand font-semibold text-white' : 'bg-sand text-muted'
        }`

    return (
        <div className="min-h-screen">
            <header className="border-b border-line bg-white">
                <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4">
                    <div className="flex h-full items-center gap-8">
                        <Logo />
                        <nav className="hidden h-full gap-6 md:flex">
                            {links.map((l) => (
                                <NavLink key={l.to} to={l.to} className={desktopClass}>
                                    {l.label}
                                </NavLink>
                            ))}
                        </nav>
                    </div>

                    <div className="flex items-center gap-3 text-sm">
                        <Link to="/app/profile" className="flex items-center gap-3">
                            <span className="hidden sm:inline">{name}</span>
                            <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-soft text-sm font-semibold text-brand">
                                {name.charAt(0)}
                            </span>
                        </Link>
                        <button onClick={handleLogout} className="text-xs font-semibold text-brand">
                            Log out
                        </button>
                    </div>
                </div>

                <nav className="flex gap-2 overflow-x-auto border-t border-line px-4 py-2 md:hidden">
                    {links.map((l) => (
                        <NavLink key={l.to} to={l.to} className={mobileClass}>
                            {l.label}
                        </NavLink>
                    ))}
                </nav>
            </header>

            <main className="mx-auto max-w-6xl px-4 py-8">
                <Outlet />
            </main>
        </div>
    )
}