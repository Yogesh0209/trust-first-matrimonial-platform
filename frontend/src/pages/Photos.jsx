import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'
import { checkImageFile, resolveMediaUrl, uploadImage } from '../utils/media'

export default function Photos() {
  const [photos, setPhotos] = useState([])
  const [hasProfile, setHasProfile] = useState(true)
  const [loading, setLoading] = useState(true)
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

  const handleFile = (e) => {
    const file = e.target.files?.[0]
    e.target.value = '' // allow choosing the same file again
    if (!file) return
    const msg = checkImageFile(file)
    if (msg) {
      setError(msg)
      return
    }
    run(async () => {
      const url = await uploadImage(file, 'photo')
      await api.post('/api/profiles/me/photos', { photoUrl: url })
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

  const full = photos.length >= 6

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-3xl font-bold">Photos</h1>
      <p className="mb-6 text-sm text-muted">
        Add up to 6 photos (JPG, PNG or WEBP, max 5 MB each). The primary photo is shown first in search results.
      </p>

      {error && (
        <p className="mb-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
      )}

      <div className="card mb-6 flex items-center justify-between gap-3 p-4">
        <p className="text-sm text-muted">
          {full ? 'You have reached the 6 photo limit.' : 'Choose a photo from your computer.'}
        </p>
        <label
          className="btn-primary cursor-pointer text-center"
          style={{ width: 'auto', paddingInline: 24, opacity: busy || full ? 0.6 : 1, pointerEvents: busy || full ? 'none' : 'auto' }}
        >
          {busy ? 'Uploading...' : 'Upload photo'}
          <input
            type="file"
            accept="image/jpeg,image/png,image/webp"
            className="hidden"
            disabled={busy || full}
            onChange={handleFile}
          />
        </label>
      </div>

      {photos.length === 0 ? (
        <div className="card p-8 text-center text-sm text-muted">No photos yet.</div>
      ) : (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
          {photos.map((p) => (
            <div key={p.id} className="card overflow-hidden">
              <div className="relative">
                <img src={resolveMediaUrl(p.photoUrl)} alt="Profile" className="h-36 w-full bg-sand object-cover" />
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