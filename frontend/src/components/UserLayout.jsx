import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import Logo from './Logo'
import NotificationBell from './NotificationBell'
import { useCallback, useEffect, useRef, useState } from 'react'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'
import { displayName } from '../utils/roles'

const links = [
  { to: '/app/search', label: 'Search' },
  { to: '/app/interests', label: 'Interests' },
  { to: '/app/chat', label: 'Messages' },
  { to: '/app/plans', label: 'Plans' },
  { to: '/app/verification', label: 'Verification' },
]

export default function UserLayout() {
  const { email, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const name = displayName(email)
  const [isInactive, setIsInactive] = useState(false)
  const [confirming, setConfirming] = useState(false)
  const [confirmLogout, setConfirmLogout] = useState(false)

  const logoutTriggerRef = useRef(null)
  const dialogRef = useRef(null)
  const backgroundRef = useRef(null)

  useEffect(() => {
    api.get('/api/profiles/me')
      .then((res) => {
        if (res.data?.visibility === 'HIDDEN') {
          setIsInactive(true)
        }
      })
      .catch((err) => {
        if (err.response?.status === 404) {
          const allowedWithoutProfile = ['/app/profile', '/app/settings', '/app/plans']
          if (!allowedWithoutProfile.includes(location.pathname)) {
            navigate('/app/profile', { replace: true })
          }
        }
      })
  }, [navigate, location.pathname])

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

  const handleConfirmActive = async () => {
    setConfirming(true)
    try {
      await api.post('/api/profiles/me/confirm-active')
      setIsInactive(false)
    } catch {
      // keep banner if error occurs
    } finally {
      setConfirming(false)
    }
  }

  const handleLogout = (e) => {
    logoutTriggerRef.current = e?.currentTarget || null
    setConfirmLogout(true)
  }

  const performLogout = () => {
    setConfirmLogout(false)
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
      {/* Background container made inert while dialog is open */}
      <div ref={backgroundRef} inert={confirmLogout}>
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
              <NotificationBell />
              <Link to="/app/profile" className="flex items-center gap-2">
                <span className="hidden sm:inline">{name}</span>
                <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-soft text-sm font-semibold text-brand">
                  {name.charAt(0)}
                </span>
              </Link>
              <Link to="/app/settings" className="text-xs font-semibold text-muted hover:text-ink">
                Settings
              </Link>
              <button
                type="button"
                onClick={handleLogout}
                className="text-xs font-semibold text-brand hover:underline cursor-pointer"
              >
                Log out
              </button>
            </div>
          </div>

          <nav className="flex gap-2 overflow-x-auto border-t border-line px-4 py-2 md:hidden items-center">
            {links.map((l) => (
              <NavLink key={l.to} to={l.to} className={mobileClass}>
                {l.label}
              </NavLink>
            ))}
            <button
              type="button"
              onClick={handleLogout}
              className="whitespace-nowrap rounded-full px-3 py-1 text-xs bg-brand-soft font-semibold text-brand cursor-pointer shrink-0"
            >
              Log out
            </button>
          </nav>
        </header>

        {isInactive && (
          <div className="border-b border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
            <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-4">
              <div className="flex items-center gap-2">
                <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-warn text-xs font-bold text-white">
                  !
                </span>
                <span className="font-medium">
                  Your profile is hidden from search because you were inactive for 30 days.
                </span>
              </div>
              <button
                type="button"
                onClick={handleConfirmActive}
                disabled={confirming}
                className="btn-primary shrink-0"
                style={{ width: 'auto', paddingBlock: 6, paddingInline: 16, fontSize: 13 }}
              >
                {confirming ? 'Reactivating...' : 'Confirm I am active'}
              </button>
            </div>
          </div>
        )}

        <main className="mx-auto max-w-6xl px-4 py-8">
          <Outlet />
        </main>
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
            aria-labelledby="user-logout-title"
            aria-describedby="user-logout-desc"
            className="card w-full max-w-sm p-6 space-y-4 shadow-xl"
          >
            <div className="flex items-center justify-between border-b border-line pb-3">
              <h2 id="user-logout-title" className="text-lg font-bold text-ink">
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
            <p id="user-logout-desc" className="text-sm text-muted">
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
