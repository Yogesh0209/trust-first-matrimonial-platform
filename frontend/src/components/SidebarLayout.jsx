import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import Logo from './Logo'
import NotificationBell from './NotificationBell'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'
import { displayName } from '../utils/roles'

export default function SidebarLayout({ badge, items }) {
  const { email, logout } = useAuth()
  const navigate = useNavigate()
  const name = displayName(email)
  const [planExpired, setPlanExpired] = useState(false)
  const [confirmLogout, setConfirmLogout] = useState(false)

  const logoutTriggerRef = useRef(null)
  const dialogRef = useRef(null)
  const backgroundRef = useRef(null)

  useEffect(() => {
    if (badge === 'Host') {
      api
        .get('/api/host/subscription')
        .then((res) => {
          if (res.data?.status === 'EXPIRED') {
            setPlanExpired(true)
          }
        })
        .catch((err) => {
          const msg = (err.response?.data?.message || '').toLowerCase()
          if (msg.includes('expired')) {
            setPlanExpired(true)
          }
        })
    }
  }, [badge])

  const cancelLogout = useCallback(() => {
    setConfirmLogout(false)
    if (backgroundRef.current) {
      backgroundRef.current.inert = false
    }
    // Restore focus to original trigger once inert state is cleared
    requestAnimationFrame(() => {
      logoutTriggerRef.current?.focus()
    })
  }, [])

  // Accessibility: Focus trap, Escape key, background scroll lock, and inert background
  useEffect(() => {
    if (!confirmLogout) return

    const bgContainer = backgroundRef.current
    if (bgContainer) {
      bgContainer.inert = true
    }
    const prevOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    const focusableSelector =
      'button:not(:disabled), [href], input:not(:disabled), select:not(:disabled), textarea:not(:disabled), [tabindex]:not([tabindex="-1"])'

    const isVisible = (el) => {
      return (
        !el.hidden &&
        el.getAttribute('aria-hidden') !== 'true' &&
        (el.offsetWidth > 0 || el.offsetHeight > 0 || el.getClientRects().length > 0) &&
        window.getComputedStyle(el).visibility !== 'hidden' &&
        window.getComputedStyle(el).display !== 'none'
      )
    }

    const getFocusables = (container) => {
      if (!container) return []
      return Array.from(container.querySelectorAll(focusableSelector)).filter(isVisible)
    }

    // Set initial focus to Cancel button inside dialog
    const timer = setTimeout(() => {
      const focusables = getFocusables(dialogRef.current)
      if (focusables.length > 0) {
        const cancelButton =
          focusables.find((el) => el.textContent?.trim().toLowerCase() === 'cancel') ||
          focusables[0]
        cancelButton.focus()
      }
    }, 0)

    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        e.preventDefault()
        cancelLogout()
        return
      }

      if (e.key === 'Tab') {
        const dialog = dialogRef.current
        if (!dialog) return
        const focusables = getFocusables(dialog)
        if (focusables.length === 0) {
          e.preventDefault()
          return
        }

        const first = focusables[0]
        const last = focusables[focusables.length - 1]

        if (e.shiftKey) {
          if (document.activeElement === first || !dialog.contains(document.activeElement)) {
            e.preventDefault()
            last.focus()
          }
        } else {
          if (document.activeElement === last || !dialog.contains(document.activeElement)) {
            e.preventDefault()
            first.focus()
          }
        }
      }
    }

    window.addEventListener('keydown', handleKeyDown)

    return () => {
      clearTimeout(timer)
      window.removeEventListener('keydown', handleKeyDown)
      if (bgContainer) {
        bgContainer.inert = false
      }
      document.body.style.overflow = prevOverflow
    }
  }, [confirmLogout, cancelLogout])

  const handleLogout = (e) => {
    logoutTriggerRef.current = e?.currentTarget || null
    setConfirmLogout(true)
  }

  const performLogout = () => {
    setConfirmLogout(false)
    logout()
    navigate('/login', { replace: true })
  }

  const sideClass = ({ isActive }) =>
    `rounded-lg px-3 py-2 text-sm ${
      isActive ? 'bg-brand-soft font-semibold text-brand' : 'text-muted hover:bg-surface'
    }`

  const mobileClass = ({ isActive }) =>
    `whitespace-nowrap rounded-full px-3 py-1.5 text-xs font-medium min-h-[36px] flex items-center ${
      isActive ? 'bg-brand font-semibold text-white' : 'bg-sand text-muted'
    }`

  return (
    <div className="min-h-screen">
      {/* Background container made inert while dialog is open */}
      <div ref={backgroundRef} inert={confirmLogout}>
        <header className="flex h-16 items-center justify-between border-b border-line bg-white px-4">
          <div className="flex items-center gap-3">
            <Logo />
            <span className="rounded-full bg-brand-soft px-2 py-0.5 text-xs font-semibold text-brand">
              {badge}
            </span>
          </div>
          <div className="flex items-center gap-3 text-sm">
            <NotificationBell />
            <span className="hidden sm:inline">{name}</span>
            <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-soft text-sm font-semibold text-brand">
              {name.charAt(0)}
            </span>
            <button
              type="button"
              onClick={handleLogout}
              aria-label="Log out"
              className="rounded px-2 py-1 text-xs font-semibold text-brand md:hidden hover:bg-brand-soft cursor-pointer"
            >
              Log out
            </button>
          </div>
        </header>

        {/* Plan Expired Banner (Design 19) */}
        {planExpired && (
          <div className="border-b border-red-200 bg-red-50 px-4 py-3 text-sm text-red-900">
            <div className="mx-auto flex flex-wrap items-center justify-between gap-4">
              <div className="flex items-center gap-2">
                <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-red-600 text-xs font-bold text-white">
                  !
                </span>
                <span className="font-medium">
                  Your plan has expired. Members are hidden from search.
                </span>
              </div>
              <Link
                to="/host/billing"
                className="rounded-lg bg-brand px-4 py-1.5 text-xs font-semibold text-white hover:bg-brand-dark"
              >
                Renew plan
              </Link>
            </div>
          </div>
        )}

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
            <button
              type="button"
              onClick={handleLogout}
              className="px-3 py-2 text-left text-xs font-semibold text-brand hover:underline cursor-pointer"
            >
              Log out
            </button>
          </aside>

          <main className="min-w-0 flex-1 p-4 sm:p-6">
            <Outlet />
          </main>
        </div>
      </div>

      {/* Logout Confirmation Dialog with Focus Trap */}
      {confirmLogout && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
          onClick={(e) => {
            if (e.target === e.currentTarget) cancelLogout()
          }}
        >
          <div
            ref={dialogRef}
            role="dialog"
            aria-modal="true"
            aria-labelledby="sidebar-logout-title"
            aria-describedby="sidebar-logout-desc"
            className="card w-full max-w-sm p-6 space-y-4 shadow-xl"
          >
            <div className="flex items-center justify-between border-b border-line pb-3">
              <h2 id="sidebar-logout-title" className="text-lg font-bold text-ink">
                Confirm Log out
              </h2>
              <button
                type="button"
                onClick={cancelLogout}
                className="text-muted hover:text-ink text-sm font-bold cursor-pointer"
                aria-label="Close dialog"
              >
                ✕
              </button>
            </div>
            <p id="sidebar-logout-desc" className="text-sm text-muted">
              Are you sure you want to log out of your account?
            </p>
            <div className="flex justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={cancelLogout}
                className="btn-outline text-xs cursor-pointer"
                style={{ paddingBlock: 8, paddingInline: 16 }}
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={performLogout}
                className="btn-primary text-xs cursor-pointer"
                style={{ width: 'auto', paddingBlock: 8, paddingInline: 20 }}
              >
                Log out
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
