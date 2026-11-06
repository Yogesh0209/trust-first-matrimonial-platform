import { useEffect, useRef, useState } from 'react'
import api from '../api/axios'
import { timeAgo } from '../utils/time'
import { getErrorMessage } from '../utils/errors'

export default function NotificationBell() {
  const [open, setOpen] = useState(false)
  const [unread, setUnread] = useState(0)
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const bellRef = useRef(null)

  // Fetch unread count
  const fetchUnreadCount = async () => {
    try {
      const res = await api.get('/api/notifications/unread-count')
      setUnread(typeof res.data.unread === 'number' ? res.data.unread : 0)
    } catch {
      // Ignore background poll errors silently
    }
  }

  // Fetch full notifications list when dropdown opens
  const fetchNotifications = async () => {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/api/notifications')
      setNotifications(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  // Poll count every 30 seconds
  useEffect(() => {
    fetchUnreadCount()
    const timer = setInterval(fetchUnreadCount, 30000)
    return () => clearInterval(timer)
  }, [])

  // Close when clicking outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (bellRef.current && !bellRef.current.contains(e.target)) {
        setOpen(false)
      }
    }
    if (open) {
      document.addEventListener('mousedown', handleClickOutside)
    }
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [open])

  const toggleDropdown = () => {
    if (!open) {
      fetchNotifications()
    }
    setOpen((prev) => !prev)
  }

  const markAsRead = async (item) => {
    if (item.read) return
    try {
      await api.put(`/api/notifications/${item.id}/read`)
      setNotifications((prev) =>
        prev.map((n) => (n.id === item.id ? { ...n, read: true } : n))
      )
      setUnread((c) => Math.max(0, c - 1))
    } catch {
      // ignore individual error
    }
  }

  const markAllAsRead = async () => {
    try {
      await api.put('/api/notifications/read-all')
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })))
      setUnread(0)
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  return (
    <div className="relative" ref={bellRef}>
      <button
        type="button"
        onClick={toggleDropdown}
        aria-label="Notifications"
        className="relative flex h-9 w-9 items-center justify-center rounded-full text-muted hover:bg-sand/60 hover:text-ink focus:outline-none"
      >
        <svg
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>

        {unread > 0 && (
          <span className="absolute -top-0.5 -right-0.5 flex h-4 min-w-[16px] items-center justify-center rounded-full bg-brand px-1 text-[10px] font-bold text-white leading-none">
            {unread > 99 ? '99+' : unread}
          </span>
        )}
      </button>

      {open && (
        <div className="card absolute right-0 mt-2 w-80 sm:w-96 overflow-hidden shadow-lg z-50">
          <div className="flex items-center justify-between border-b border-line px-4 py-3 bg-surface">
            <h3 className="text-sm font-semibold text-ink">Notifications</h3>
            {unread > 0 && (
              <button
                type="button"
                onClick={markAllAsRead}
                className="text-xs font-semibold text-brand hover:underline"
              >
                Mark all as read
              </button>
            )}
          </div>

          <div className="max-h-80 overflow-y-auto divide-y divide-line">
            {loading && (
              <p className="p-4 text-center text-xs text-muted">Loading notifications...</p>
            )}

            {error && (
              <p className="m-3 rounded bg-red-50 p-2 text-xs text-red-600">{error}</p>
            )}

            {!loading && !error && notifications.length === 0 && (
              <p className="p-6 text-center text-xs text-muted">No notifications yet.</p>
            )}

            {!loading &&
              notifications.map((n) => (
                <div
                  key={n.id}
                  onClick={() => markAsRead(n)}
                  className={`cursor-pointer px-4 py-3 text-left transition hover:bg-surface ${
                    n.read ? 'bg-white' : 'bg-brand-soft/30 font-medium'
                  }`}
                >
                  <div className="flex items-start justify-between gap-2">
                    <p className="text-xs text-ink leading-relaxed">{n.message}</p>
                    {!n.read && (
                      <span className="mt-1 h-2 w-2 shrink-0 rounded-full bg-brand" />
                    )}
                  </div>
                  <span className="mt-1 block text-[11px] text-muted">
                    {timeAgo(n.createdAt)}
                  </span>
                </div>
              ))}
          </div>
        </div>
      )}
    </div>
  )
}

