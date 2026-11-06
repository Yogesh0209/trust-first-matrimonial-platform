import { useState } from 'react'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'

const REASONS = [
  ['FAKE_PHOTOS', 'Fake photos'],
  ['ASKED_FOR_MONEY', 'Asked for money'],
  ['INACTIVE_PROFILE', 'Inactive profile'],
  ['WRONG_DETAILS', 'Wrong details'],
  ['OTHER', 'Other'],
]

export default function ReportModal({ profileId, onClose, onDone }) {
  const [reason, setReason] = useState('FAKE_PHOTOS')
  const [details, setDetails] = useState('')
  const [error, setError] = useState('')
  const [sending, setSending] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSending(true)
    try {
      await api.post('/api/reports', {
        reportedProfileId: profileId,
        reason,
        details: details.trim() || undefined,
      })
      onDone()
      onClose()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <form onSubmit={handleSubmit} className="card w-full max-w-md p-6">
        <div className="flex items-start justify-between">
          <h2 className="text-xl font-bold">Report this profile</h2>
          <button type="button" onClick={onClose} aria-label="Close"
            className="flex h-7 w-7 items-center justify-center rounded-full bg-sand text-muted">
            ×
          </button>
        </div>
        <p className="mb-4 mt-1 text-xs text-muted">Tell us what is wrong. We review every report.</p>

        {error && (
          <p className="mb-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
        )}

        <div className="mb-4 space-y-3 text-sm">
          {REASONS.map(([value, label]) => (
            <label key={value} className="flex items-center gap-2">
              <input type="radio" name="reason" className="accent-brand"
                checked={reason === value} onChange={() => setReason(value)} />
              {label}
            </label>
          ))}
        </div>

        <label className="label">Details (optional)</label>
        <textarea rows={3} maxLength={500} className="input"
          placeholder="Add anything that helps our team review"
          value={details} onChange={(e) => setDetails(e.target.value)} />
        <p className="mb-4 mt-1 text-right text-xs text-muted">{details.length}/500</p>

        <div className="flex justify-between">
          <button type="button" onClick={onClose} className="btn-outline">Cancel</button>
          <button type="submit" disabled={sending} className="btn-primary"
            style={{ width: 'auto', paddingInline: 24 }}>
            {sending ? 'Sending...' : 'Submit report'}
          </button>
        </div>
      </form>
    </div>
  )
}