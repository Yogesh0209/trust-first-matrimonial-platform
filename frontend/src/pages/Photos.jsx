import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'

export default function Photos() {
  const [photos, setPhotos] = useState([])
  const [hasProfile, setHasProfile] = useState(true)
  const [loading, setLoading] = useState(true)
  const [url, setUrl] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const load = async () => {
    try {
      const res = await api.get('/api/profiles/me')
      setPhotos(res.data.photos || [])
      setHasProfile(true)
    } catch (err) {
      if (err.response?.status === 404) setHasProfile(false)
      else setError(getErrorMessage(err))
    }
  }

  useEffect(() => {
    load().finally(() => setLoading(false))
  }, [])

  // run an action, then reload the photo list
  const run = async (action) => {
    setError('')
    setBusy(true)
    try {
      await action()
      await load()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  const handleAdd = (e) => {
    e.preventDefault()
    run(async () => {
      await api.post('/api/profiles/me/photos', { photoUrl: url.trim() })
      setUrl('')
    })
  }

  const makePrimary = (id) => run(() => api.put(`/api/profiles/me/photos/${id}/primary`))
  const remove = (id) => run(() => api.delete(`/api/profiles/me/photos/${id}`))

  if (loading) return <p className="text-sm text-muted">Loading...</p>

  if (!hasProfile) {
    return (
      <div className="card mx-auto max-w-md p-8 text-center">
        <h1 className="text-2xl font-bold">Create your profile first</h1>
        <p className="mt-2 text-sm text-muted">You can add photos after your profile is saved.</p>
        <Link to="/app/profile" className="btn-primary mt-6 block text-center">Go to profile</Link>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-3xl font-bold">Photos</h1>
      <p className="mb-6 text-sm text-muted">
        Add up to 6 photos. The primary photo is shown first in search results.
      </p>

      {error && (
        <p className="mb-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
      )}

      <form onSubmit={handleAdd} className="card mb-6 flex flex-col gap-3 p-4 sm:flex-row">
        <input
          required
          type="url"
          className="input"
          placeholder="Paste an image link, e.g. https://example.com/photo.jpg"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
        />
        <button type="submit" disabled={busy} className="btn-primary" style={{ width: 'auto', paddingInline: 24 }}>
          Add photo
        </button>
      </form>

      {photos.length === 0 ? (
        <div className="card p-8 text-center text-sm text-muted">No photos yet.</div>
      ) : (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
          {photos.map((p) => (
            <div key={p.id} className="card overflow-hidden">
              <div className="relative">
                <img src={p.photoUrl} alt="Profile" className="h-36 w-full bg-sand object-cover" />
                {p.primary && (
                  <span className="absolute left-2 top-2 rounded-full bg-brand px-2 py-0.5 text-[11px] font-semibold text-white">
                    Primary
                  </span>
                )}
              </div>
              <div className="flex justify-between gap-2 p-2 text-xs font-semibold">
                {p.primary ? (
                  <span className="text-muted">Main photo</span>
                ) : (
                  <button disabled={busy} onClick={() => makePrimary(p.id)} className="text-brand">
                    Make primary
                  </button>
                )}
                <button disabled={busy} onClick={() => remove(p.id)} className="text-red-600">
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <p className="mt-6 text-xs text-muted">{photos.length} of 6 photos used.</p>
      <Link to="/app/profile" className="mt-4 inline-block text-sm font-semibold text-brand">
        ← Back to profile
      </Link>
    </div>
  )
}