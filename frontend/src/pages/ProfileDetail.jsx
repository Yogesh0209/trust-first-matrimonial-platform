import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'
import TrustBadge from '../components/TrustBadge'
import InterestModal from '../components/InterestModal'
import ReportModal from '../components/ReportModal'
import { getErrorMessage } from '../utils/errors'

const MARITAL = { NEVER_MARRIED: 'Never married', DIVORCED: 'Divorced', WIDOWED: 'Widowed' }

function heightText(cm) {
  if (!cm) return 'Not set'
  const inches = Math.round(cm / 2.54)
  return `${cm} cm (${Math.floor(inches / 12)} ft ${inches % 12} in)`
}

function Check({ ok, children }) {
  return (
    <li className="flex items-center gap-2 text-sm">
      <span
        className={`flex h-5 w-5 items-center justify-center rounded-full text-[11px] ${
          ok ? 'bg-green-50 text-ok' : 'bg-sand text-muted'
        }`}
      >
        {ok ? '✓' : '○'}
      </span>
      <span className={ok ? '' : 'text-muted'}>{children}</span>
    </li>
  )
}

function Detail({ label, value }) {
  return (
    <div>
      <p className="text-xs text-muted">{label}</p>
      <p className="text-sm font-semibold">{value || 'Not set'}</p>
    </div>
  )
}

export default function ProfileDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [unavailable, setUnavailable] = useState('')
  const [loading, setLoading] = useState(true)
  const [shortlisted, setShortlisted] = useState(false)
  const [photoIdx, setPhotoIdx] = useState(0)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [showInterest, setShowInterest] = useState(false)
  const [showReport, setShowReport] = useState(false)

  useEffect(() => {
    setLoading(true)
    Promise.all([
      api.get(`/api/profiles/${id}`),
      api.get('/api/shortlists').catch(() => ({ data: [] })),
    ])
      .then(([p, s]) => {
        setProfile(p.data)
        setShortlisted(s.data.some((x) => String(x.profileId) === String(id)))
      })
      .catch((err) => setUnavailable(err.response?.data?.message || 'This profile is not available'))
      .finally(() => setLoading(false))
  }, [id])

  const flash = (text) => {
    setNotice(text)
    setTimeout(() => setNotice(''), 3000)
  }

  const toggleShortlist = async () => {
    setError('')
    try {
      if (shortlisted) await api.delete(`/api/shortlists/${id}`)
      else await api.post(`/api/shortlists/${id}`)
      setShortlisted(!shortlisted)
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const handleBlock = async () => {
    if (!window.confirm(`Block ${profile.fullName}? You will no longer see each other.`)) return
    setError('')
    try {
      await api.post(`/api/blocks/${id}`)
      navigate('/app/search')
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  if (loading) return <p className="text-sm text-muted">Loading...</p>

  if (unavailable) {
    return (
      <div className="card mx-auto max-w-md p-8 text-center">
        <h1 className="text-2xl font-bold">Profile not available</h1>
        <p className="mt-2 text-sm text-muted">{unavailable}</p>
        <Link to="/app/search" className="btn-primary mt-6 block text-center">Back to search</Link>
      </div>
    )
  }

  const photos = profile.photos || []
  const mainPhoto = photos[photoIdx]?.photoUrl || profile.primaryPhotoUrl
  const level = profile.trustLevel

  return (
    <div className="grid gap-8 lg:grid-cols-[320px_1fr]">
      {/* Photos */}
      <div>
        <div className="aspect-[3/4] overflow-hidden rounded-2xl bg-brand-soft">
          {mainPhoto ? (
            <img src={mainPhoto} alt={profile.fullName} className="h-full w-full object-cover" />
          ) : (
            <div className="flex h-full items-end justify-center">
              <div className="h-40 w-40 rounded-t-full bg-[#dcc8cd]" />
            </div>
          )}
        </div>
        {photos.length > 1 && (
          <div className="mt-3 grid grid-cols-3 gap-3">
            {photos.map((p, i) => (
              <button key={p.id} onClick={() => setPhotoIdx(i)}
                className={`h-20 overflow-hidden rounded-xl border-2 ${
                  i === photoIdx ? 'border-brand' : 'border-transparent'
                }`}>
                <img src={p.photoUrl} alt="" className="h-full w-full object-cover" />
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Details */}
      <div>
        <h1 className="text-3xl font-bold">
          {profile.fullName}, {profile.age}
        </h1>
        <p className="mt-1 text-sm text-muted">
          {[profile.city, profile.religion, profile.occupation].filter(Boolean).join(' | ')}
        </p>
        <div className="mt-3">
          <TrustBadge level={level} />
        </div>

        {notice && (
          <p className="mt-4 rounded-lg bg-green-50 px-3 py-2 text-sm text-ok">{notice}</p>
        )}
        {error && (
          <p className="mt-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
        )}

        <div className="card mt-5 p-5">
          <h2 className="mb-3 text-base font-bold">Trust summary</h2>
          <ul className="grid gap-3 sm:grid-cols-2">
            <Check ok={level >= 1}>Email verified</Check>
            <Check ok={level >= 3}>Government ID verified</Check>
            <Check ok={level >= 1}>Phone verified</Check>
            <Check ok>
              Trust score {profile.trustScore ?? 0} ({profile.trustBand})
            </Check>
            <Check ok={level >= 2}>Selfie verified</Check>
            <Check ok>
              Profile updated{' '}
              {profile.updatedAt ? new Date(profile.updatedAt).toLocaleDateString() : '-'}
            </Check>
          </ul>
        </div>

        <div className="card mt-4 p-5">
          <h2 className="mb-2 text-base font-bold">About</h2>
          <p className="text-sm text-muted">{profile.about || 'No description added yet.'}</p>
        </div>

        <div className="card mt-4 p-5">
          <h2 className="mb-3 text-base font-bold">Basic details</h2>
          <div className="grid gap-4 sm:grid-cols-2">
            <Detail label="Height" value={heightText(profile.heightCm)} />
            <Detail label="Education" value={profile.education} />
            <Detail label="Family type" value={profile.familyType} />
            <Detail label="Mother tongue" value={profile.motherTongue} />
            <Detail label="Marital status" value={MARITAL[profile.maritalStatus]} />
            <Detail label="Occupation" value={profile.occupation} />
          </div>
        </div>

        <div className="mt-6 flex flex-wrap items-center gap-3">
          <button onClick={() => setShowInterest(true)} className="btn-primary"
            style={{ width: 'auto', paddingInline: 28 }}>
            Send Interest
          </button>
          <button onClick={toggleShortlist} className="btn-outline">
            {shortlisted ? '♥ Shortlisted' : '♡ Shortlist'}
          </button>
          <button onClick={handleBlock} className="btn-outline">Block</button>
          <button onClick={() => setShowReport(true)}
            className="btn-outline ml-auto border-red-300 text-red-600">
            Report profile
          </button>
        </div>
      </div>

      {showInterest && (
        <InterestModal
          target={{ profileId: profile.id, fullName: profile.fullName }}
          onClose={() => setShowInterest(false)}
          onSent={() => flash('Interest sent.')}
        />
      )}
      {showReport && (
        <ReportModal
          profileId={profile.id}
          onClose={() => setShowReport(false)}
          onDone={() => flash('Report submitted. Thank you.')}
        />
      )}
    </div>
  )
}