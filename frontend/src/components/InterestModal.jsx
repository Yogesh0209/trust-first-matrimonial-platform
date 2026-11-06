import { useState } from 'react'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'

export default function InterestModal({ target, onClose, onSent }) {
  const [note, setNote] = useState('')
  const [error, setError] = useState('')
  const [sending, setSending] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSending(true)
    try {
      await api.post('/api/interests', {
        receiverProfileId: target.profileId,
        note: note.trim() || undefined,
      })
      onSent(target.profileId)
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
        <h2 className="text-xl font-bold">Send interest to {target.fullName}</h2>
        <p className="mb-4 mt-1 text-xs text-muted">
          Add a short note (optional). They can accept or decline.
        </p>

        {error && (
          <p className="mb-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
        )}

        <textarea
          rows={4}
          maxLength={200}
          className="input"
          placeholder="e.g. Hello, I liked your profile."
          value={note}
          onChange={(e) => setNote(e.target.value)}
        />
        <p className="mb-4 mt-1 text-right text-xs text-muted">{note.length}/200</p>

        <div className="flex justify-end gap-3">
          <button type="button" onClick={onClose} className="btn-outline">Cancel</button>
          <button type="submit" disabled={sending} className="btn-primary"
            style={{ width: 'auto', paddingInline: 24 }}>
            {sending ? 'Sending...' : 'Send interest'}
          </button>
        </div>
      </form>
    </div>
  )
}