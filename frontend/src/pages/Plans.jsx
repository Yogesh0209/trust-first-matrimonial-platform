import { useEffect, useState } from 'react'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'

export default function Plans() {
  const [bureau, setBureau] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api
      .get('/api/me/bureau')
      .then((res) => setBureau(res.data))
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="text-sm text-muted">Loading...</p>

  if (error) {
    return (
      <p className="mx-auto mt-6 max-w-md rounded bg-red-50 p-2 text-center text-xs text-red-600">
        {error}
      </p>
    )
  }

  const features = [
    { label: 'Level 2 selfie verification', on: true },
    { label: 'Level 3 Government ID verification', on: bureau.govIdVerification },
    { label: 'Cross-bureau network search', on: bureau.crossBureauSearch },
  ]

  return (
    <div className="mx-auto max-w-3xl space-y-6 py-4">
      <div className="text-center">
        <h1 className="text-3xl font-bold sm:text-4xl">My Bureau</h1>
        <p className="mt-2 text-sm text-muted">
          Your membership and features are provided by your marriage bureau.
        </p>
      </div>

      {/* Bureau + plan */}
      <div className="card p-6 sm:p-8">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h2 className="text-xl font-bold text-ink">{bureau.name}</h2>
            <p className="text-sm text-muted">
              {bureau.city} • Code {bureau.bureauCode}
            </p>
          </div>
          <span className="rounded-full bg-brand-soft px-3 py-1 text-xs font-semibold text-brand">
            {bureau.planName ? `${bureau.planName} plan` : 'No active plan'}
          </span>
        </div>

        <h3 className="mt-6 text-sm font-semibold text-ink">What your bureau plan includes</h3>
        <div className="mt-3 space-y-3 text-sm">
          {features.map((f) => (
            <div key={f.label} className="flex items-center gap-2.5">
              {f.on ? (
                <span className="font-bold text-ok">✓</span>
              ) : (
                <span className="text-muted/60">✕</span>
              )}
              <span className={f.on ? 'text-ink' : 'text-muted'}>{f.label}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Contact */}
      <div className="card p-6 sm:p-8">
        <h3 className="text-base font-bold text-ink">Contact your bureau</h3>
        {bureau.contactEmail || bureau.contactPhone ? (
          <div className="mt-3 space-y-2 text-sm">
            {bureau.contactName && <p className="text-ink">{bureau.contactName}</p>}
            {bureau.contactEmail && (
              <p>
                <span className="text-muted">Email: </span>
                <a href={`mailto:${bureau.contactEmail}`} className="font-medium text-brand hover:underline">
                  {bureau.contactEmail}
                </a>
              </p>
            )}
            {bureau.contactPhone && (
              <p>
                <span className="text-muted">Phone: </span>
                <a href={`tel:${bureau.contactPhone}`} className="font-medium text-brand hover:underline">
                  {bureau.contactPhone}
                </a>
              </p>
            )}
          </div>
        ) : (
          <p className="mt-3 text-sm text-muted">Contact details are not available yet.</p>
        )}
      </div>
    </div>
  )
}