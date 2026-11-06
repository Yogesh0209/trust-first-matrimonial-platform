import { useEffect, useState, useCallback } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'
import { timeAgo } from '../utils/time'

export default function InactiveProfile() {
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const load = useCallback(async () => {
    try {
      const res = await api.get('/api/profiles/me')
      setProfile(res.data)
    } catch (err) {
      if (err.response?.status === 404) {
        navigate('/app/profile', { replace: true })
      } else {
        setError(getErrorMessage(err))
      }
    } finally {
      setLoading(false)
    }
  }, [navigate])

  useEffect(() => {
    load()
  }, [load])

  const handleReactivate = async () => {
    setBusy(true)
    setError('')
    try {
      await api.post('/api/profiles/me/confirm-active')
      setSuccess('Your profile is now active and visible in search results!')
      setTimeout(() => {
        navigate('/app/search', { replace: true })
      }, 1500)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  if (loading) {
    return <p className="text-sm text-muted">Checking profile status...</p>
  }

  return (
    <div className="mx-auto max-w-xl space-y-6">
      {/* Top alert banner */}
      <div className="flex items-center gap-3 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900 shadow-xs">
        <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-warn text-xs font-bold text-white">
          !
        </span>
        <p className="font-medium">
          Your profile is hidden from search because you were inactive for 30 days.
        </p>
      </div>

      {/* Main card */}
      <div className="card p-8 sm:p-12 text-center">
        <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-amber-100 text-warn">
          <svg
            width="28"
            height="28"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
        </div>

        <h1 className="text-2xl font-bold text-ink sm:text-3xl">Your profile is hidden</h1>
        <p className="mt-2 text-sm text-muted">
          Confirm that your profile is still current to show it in search again.
        </p>

        {profile?.lastActiveAt && (
          <div className="mt-4">
            <span className="inline-block rounded-full bg-amber-50 px-3 py-1 text-xs font-medium text-warn">
              Last active: {timeAgo(profile.lastActiveAt)}
            </span>
          </div>
        )}

        {error && (
          <p className="mt-4 rounded-lg bg-red-50 p-2 text-sm text-red-600">{error}</p>
        )}
        {success && (
          <p className="mt-4 rounded-lg bg-green-50 p-2 text-sm text-ok">{success}</p>
        )}

        <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
          <button
            type="button"
            onClick={handleReactivate}
            disabled={busy}
            className="btn-primary"
            style={{ width: 'auto', paddingInline: 24 }}
          >
            {busy ? 'Reactivating...' : 'Reactivate profile'}
          </button>
          <Link to="/app/profile" className="btn-outline">
            Update details
          </Link>
        </div>

        <p className="mt-8 text-xs text-muted">
          Keeping profiles active keeps the platform trustworthy.
        </p>
      </div>
    </div>
  )
}
