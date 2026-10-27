import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'

const DOC_TYPES = ['Aadhaar', 'PAN', 'Passport', 'Voter ID', 'Driving licence']

function StatusBadge({ item }) {
  if (!item) return null
  const styles = {
    PENDING: ['Pending review', 'bg-amber-50 text-warn'],
    APPROVED: ['Approved', 'bg-green-50 text-ok'],
    REJECTED: ['Rejected', 'bg-red-50 text-red-600'],
  }
  const [label, cls] = styles[item.status]
  return <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${cls}`}>{label}</span>
}

export default function Verification() {
  const [profile, setProfile] = useState(null)
  const [items, setItems] = useState([])
  const [noProfile, setNoProfile] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [selfieUrl, setSelfieUrl] = useState('')
  const [idForm, setIdForm] = useState({
    documentType: 'Aadhaar',
    fileUrl: '',
    fileUrlBack: '',
    maskedReference: '',
  })

  const load = async () => {
    try {
      const [p, v] = await Promise.all([
        api.get('/api/profiles/me'),
        api.get('/api/verification/me'),
      ])
      setProfile(p.data)
      setItems(v.data)
      setNoProfile(false)
    } catch (err) {
      if (err.response?.status === 404) setNoProfile(true)
      else setError(getErrorMessage(err))
    }
  }

  useEffect(() => {
    load().finally(() => setLoading(false))
  }, [])

  const latest = (type) => items.find((i) => i.type === type)
  const canSubmit = (item) => !item || item.status === 'REJECTED'

  const submit = async (url, body, after) => {
    setError('')
    setBusy(true)
    try {
      await api.post(url, body)
      after()
      await load()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  const submitSelfie = (e) => {
    e.preventDefault()
    submit('/api/verification/selfie', { fileUrl: selfieUrl.trim() }, () => setSelfieUrl(''))
  }

  const submitId = (e) => {
    e.preventDefault()
    const body = {
      documentType: idForm.documentType,
      fileUrl: idForm.fileUrl.trim(),
      fileUrlBack: idForm.fileUrlBack.trim(),
      maskedReference: idForm.maskedReference.trim(),
    }
    submit('/api/verification/id', body, () =>
      setIdForm({ documentType: 'Aadhaar', fileUrl: '', fileUrlBack: '', maskedReference: '' })
    )
  }

  const setId = (key) => (e) => setIdForm({ ...idForm, [key]: e.target.value })

  if (loading) return <p className="text-sm text-muted">Loading...</p>

  if (noProfile) {
    return (
      <div className="card mx-auto max-w-md p-8 text-center">
        <h1 className="text-2xl font-bold">Create your profile first</h1>
        <p className="mt-2 text-sm text-muted">Verification starts after your profile is saved.</p>
        <Link to="/app/profile" className="btn-primary mt-6 block text-center">Go to profile</Link>
      </div>
    )
  }

  const selfie = latest('SELFIE')
  const govId = latest('GOV_ID')

  return (
    <div>
      <h1 className="text-3xl font-bold">Verify your profile</h1>
      <p className="mb-6 text-sm text-muted">A higher trust level means more visibility in search.</p>

      {error && (
        <p className="mb-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
      )}

      <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
        <div className="space-y-4">
          {/* Level 1 */}
          <div className="card p-5">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-muted">Level 1</p>
                <h2 className="text-lg font-bold">Email and phone</h2>
              </div>
              <span className="rounded-full bg-green-50 px-2 py-0.5 text-xs font-semibold text-ok">
                Completed
              </span>
            </div>
          </div>

          {/* Level 2: selfie */}
          <div className="card p-5">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-muted">Level 2 Verified</p>
                <h2 className="text-lg font-bold">Selfie verification</h2>
              </div>
              <StatusBadge item={selfie} />
            </div>
            <p className="mt-1 text-xs text-muted">
              Paste a link to a clear selfie. We match it with your profile photos. Available on all plans.
            </p>
            {selfie?.status === 'REJECTED' && selfie.rejectReason && (
              <p className="mt-2 text-xs text-red-600">Reason: {selfie.rejectReason}</p>
            )}
            {canSubmit(selfie) && (
              <form onSubmit={submitSelfie} className="mt-4 flex flex-col gap-3 sm:flex-row">
                <input required type="url" className="input" placeholder="Selfie image link (https://...)"
                  value={selfieUrl} onChange={(e) => setSelfieUrl(e.target.value)} />
                <button type="submit" disabled={busy} className="btn-primary"
                  style={{ width: 'auto', paddingInline: 24 }}>
                  Submit selfie
                </button>
              </form>
            )}
          </div>

          {/* Level 3: government ID */}
          <div className="card p-5">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-muted">Level 3 Gov ID</p>
                <h2 className="text-lg font-bold">Government ID verification</h2>
              </div>
              <StatusBadge item={govId} />
            </div>
            <p className="mt-1 text-xs text-muted">
              A registered marriage bureau reviews your ID. Available on the Pro plan and above.
            </p>
            {govId?.status === 'REJECTED' && govId.rejectReason && (
              <p className="mt-2 text-xs text-red-600">Reason: {govId.rejectReason}</p>
            )}
            {canSubmit(govId) && (
              <form onSubmit={submitId} className="mt-4 grid gap-3 sm:grid-cols-2">
                <div>
                  <label className="label">Document type</label>
                  <select className="input" value={idForm.documentType} onChange={setId('documentType')}>
                    {DOC_TYPES.map((d) => (
                      <option key={d} value={d}>{d}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="label">Last 4 characters of the ID number</label>
                  <input required maxLength={4} className="input" placeholder="e.g. 4821"
                    value={idForm.maskedReference} onChange={setId('maskedReference')} />
                </div>
                <div>
                  <label className="label">ID front (image link)</label>
                  <input required type="url" className="input" placeholder="https://..."
                    value={idForm.fileUrl} onChange={setId('fileUrl')} />
                </div>
                <div>
                  <label className="label">ID back (image link, optional)</label>
                  <input type="url" className="input" placeholder="https://..."
                    value={idForm.fileUrlBack} onChange={setId('fileUrlBack')} />
                </div>
                <div className="flex items-center gap-4 sm:col-span-2">
                  <button type="submit" disabled={busy} className="btn-primary"
                    style={{ width: 'auto', paddingInline: 24 }}>
                    Upload ID
                  </button>
                  <Link to="/app/plans" className="text-sm font-semibold text-brand">Upgrade to Pro</Link>
                </div>
              </form>
            )}
          </div>
        </div>

        {/* Right: trust level summary */}
        <aside className="card h-fit p-5">
          <p className="text-xs text-muted">Your trust level</p>
          <p className="font-serif text-3xl font-bold text-brand">Level {profile.trustLevel}</p>
          <dl className="mt-4 space-y-2 text-sm">
            <div className="flex justify-between">
              <dt className="text-muted">Trust score</dt>
              <dd className="font-semibold">{profile.trustScore ?? 0}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-muted">Trust band</dt>
              <dd className="font-semibold">{profile.trustBand}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-muted">Profile updated</dt>
              <dd className="font-semibold">
                {profile.updatedAt ? new Date(profile.updatedAt).toLocaleDateString() : '-'}
              </dd>
            </div>
          </dl>
          <Link to="/app/profile" className="btn-outline mt-5 block text-center">Update profile</Link>
        </aside>
      </div>
    </div>
  )
}