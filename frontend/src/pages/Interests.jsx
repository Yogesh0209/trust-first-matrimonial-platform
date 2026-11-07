import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import TrustBadge from '../components/TrustBadge'
import { timeAgo } from '../utils/time'
import { getErrorMessage } from '../utils/errors'
import { resolveMediaUrl } from '../utils/media'

function Avatar({ url }) {
  return url ? (
    <img src={resolveMediaUrl(url)} alt="" className="h-12 w-12 rounded-full object-cover" />
  ) : (
    <div className="flex h-12 w-12 items-end justify-center overflow-hidden rounded-full bg-brand-soft">
      <div className="h-6 w-6 rounded-t-full bg-[#dcc8cd]" />
    </div>
  )
}

function StatusPill({ status }) {
  const styles = {
    PENDING: 'bg-amber-50 text-warn',
    ACCEPTED: 'bg-green-50 text-ok',
    DECLINED: 'bg-red-50 text-red-600',
    EXPIRED: 'bg-sand text-muted',
  }
  const label = status.charAt(0) + status.slice(1).toLowerCase()
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${styles[status]}`}>
      {label}
    </span>
  )
}

function Person({ photo, name, age, city, note }) {
  return (
    <div className="flex min-w-[200px] items-center gap-3">
      <Avatar url={photo} />
      <div>
        <p className="font-semibold">{name}</p>
        <p className="text-xs text-muted">
          {age ? `${age} yrs` : ''}
          {age && city ? ' | ' : ''}
          {city}
        </p>
        {note && <p className="mt-1 text-xs italic text-muted">“{note}”</p>}
      </div>
    </div>
  )
}

function Empty({ text }) {
  return <div className="card p-10 text-center text-sm text-muted">{text}</div>
}

export default function Interests() {
  const [tab, setTab] = useState('received')
  const [received, setReceived] = useState([])
  const [sent, setSent] = useState([])
  const [shortlist, setShortlist] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)

  useEffect(() => {
    Promise.allSettled([
      api.get('/api/interests/received'),
      api.get('/api/interests/sent'),
      api.get('/api/shortlists'),
    ])
      .then(([r, s, l]) => {
        if (r.status === 'fulfilled') setReceived(r.value.data)
        else setError(getErrorMessage(r.reason))
        if (s.status === 'fulfilled') setSent(s.value.data)
        if (l.status === 'fulfilled') setShortlist(l.value.data)
      })
      .finally(() => setLoading(false))
  }, [])

  const pendingCount = received.filter((i) => i.status === 'PENDING').length

  const respond = async (item, action) => {
    setBusyId(item.id)
    setError('')
    try {
      const res = await api.put(`/api/interests/${item.id}/respond`, { action })
      setReceived((list) =>
        list.map((x) => (x.id === item.id ? { ...x, status: res.data.status } : x))
      )
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusyId(null)
    }
  }

  const withdraw = async (item) => {
    setBusyId(item.id)
    setError('')
    try {
      await api.delete(`/api/interests/${item.id}`)
      setSent((list) => list.filter((x) => x.id !== item.id))
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusyId(null)
    }
  }

  const removeShortlist = async (profileId) => {
    setError('')
    try {
      await api.delete(`/api/shortlists/${profileId}`)
      setShortlist((list) => list.filter((x) => x.profileId !== profileId))
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const tabClass = (name) =>
    `flex items-center gap-2 border-b-2 pb-2 text-sm ${
      tab === name ? 'border-brand font-semibold text-brand' : 'border-transparent text-muted'
    }`

  if (loading) return <p className="text-sm text-muted">Loading...</p>

  return (
    <div>
      <h1 className="text-3xl font-bold">Interests</h1>

      <div className="mb-5 mt-5 flex gap-8 border-b border-line">
        <button onClick={() => setTab('received')} className={tabClass('received')}>
          Received
          {pendingCount > 0 && (
            <span className="rounded-full bg-brand-soft px-2 text-xs text-brand">{pendingCount}</span>
          )}
        </button>
        <button onClick={() => setTab('sent')} className={tabClass('sent')}>Sent</button>
        <button onClick={() => setTab('shortlisted')} className={tabClass('shortlisted')}>
          Shortlisted
        </button>
      </div>

      {error && (
        <p className="mb-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
      )}

      {/* Received */}
      {tab === 'received' && (
        <div className="space-y-3">
          {received.length === 0 && <Empty text="No interests received yet." />}
          {received.map((i) => (
            <div key={i.id} className="card flex flex-wrap items-center justify-between gap-4 p-4">
              <Person photo={i.photoUrl} name={i.senderName} age={i.age} city={i.city} note={i.note} />
              <TrustBadge level={i.trustLevel ?? 1} />
              <span className="text-xs text-muted">Received {timeAgo(i.createdAt)}</span>
              <div className="flex items-center gap-2">
                <Link to={`/app/profiles/${i.senderProfileId}`} className="btn-outline">View</Link>
                {i.status === 'PENDING' ? (
                  <>
                    <button disabled={busyId === i.id} onClick={() => respond(i, 'DECLINE')} className="btn-outline">
                      Decline
                    </button>
                    <button disabled={busyId === i.id} onClick={() => respond(i, 'ACCEPT')} className="btn-primary"
                      style={{ width: 'auto', paddingInline: 20 }}>
                      Accept
                    </button>
                  </>
                ) : (
                  <>
                    {i.status === 'ACCEPTED' && (
                      <Link
                        to={`/app/chat/${i.id}`}
                        className="btn-primary"
                        style={{ width: 'auto', paddingInline: 16 }}
                      >
                        Message
                      </Link>
                    )}
                    <StatusPill status={i.status} />
                  </>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Sent */}
      {tab === 'sent' && (
        <div className="space-y-3">
          {sent.length === 0 && <Empty text="You have not sent any interests yet." />}
          {sent.map((i) => (
            <div key={i.id} className="card flex flex-wrap items-center justify-between gap-4 p-4">
              <Person photo={i.photoUrl} name={i.receiverName} age={i.age} city={i.city} note={i.note} />
              <TrustBadge level={i.trustLevel ?? 1} />
              <span className="text-xs text-muted">Sent {timeAgo(i.createdAt)}</span>
              <div className="flex items-center gap-2">
                <Link to={`/app/profiles/${i.receiverProfileId}`} className="btn-outline">View</Link>
                {i.status === 'ACCEPTED' && (
                  <Link
                    to={`/app/chat/${i.id}`}
                    className="btn-primary"
                    style={{ width: 'auto', paddingInline: 16 }}
                  >
                    Message
                  </Link>
                )}
                <StatusPill status={i.status} />
                {i.status === 'PENDING' && (
                  <button disabled={busyId === i.id} onClick={() => withdraw(i)}
                    className="text-xs font-semibold text-red-600">
                    Withdraw
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Shortlisted */}
      {tab === 'shortlisted' && (
        <div className="space-y-3">
          {shortlist.length === 0 && <Empty text="Your shortlist is empty." />}
          {shortlist.map((p) => (
            <div key={p.profileId} className="card flex flex-wrap items-center justify-between gap-4 p-4">
              <Person photo={p.primaryPhotoUrl} name={p.fullName} age={p.age} city={p.city} />
              <TrustBadge level={p.trustLevel} />
              <div className="flex items-center gap-2">
                <Link to={`/app/profiles/${p.profileId}`} className="btn-outline">View</Link>
                <button onClick={() => removeShortlist(p.profileId)}
                  className="text-xs font-semibold text-red-600">
                  Remove
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
