import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import TrustBadge from '../components/TrustBadge'
import InterestModal from '../components/InterestModal'
import { activeLabel } from '../utils/time'
import { getErrorMessage } from '../utils/errors'

const PAGE_SIZE = 9
const RELIGIONS = ['Hindu', 'Muslim', 'Christian', 'Sikh', 'Jain', 'Buddhist', 'Other']
const emptyFilters = { minAge: '', maxAge: '', religion: '', minTrustLevel: '', city: '' }

export default function Search() {
  const [draft, setDraft] = useState(emptyFilters) // what the user is typing
  const [applied, setApplied] = useState(emptyFilters) // what was searched
  const [page, setPage] = useState(0)
  const [results, setResults] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [target, setTarget] = useState(null) // profile for the interest pop-up
  const [sent, setSent] = useState([]) // profile ids we sent interest to

  useEffect(() => {
    const params = { page, size: PAGE_SIZE }
    Object.entries(applied).forEach(([k, v]) => {
      if (v !== '') params[k] = v
    })
    setLoading(true)
    setError('')
    api
      .get('/api/search', { params })
      .then((res) => {
        setResults(res.data)
        setTotal(Number(res.headers['x-total-count'] ?? res.data.length))
      })
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [applied, page])

  const set = (key) => (e) => setDraft({ ...draft, [key]: e.target.value })

  const applyFilters = (e) => {
    e.preventDefault()
    setPage(0)
    setApplied(draft)
  }

  const resetFilters = () => {
    setDraft(emptyFilters)
    setPage(0)
    setApplied(emptyFilters)
  }

  const toggleShortlist = async (r) => {
    try {
      if (r.shortlisted) await api.delete(`/api/shortlists/${r.profileId}`)
      else await api.post(`/api/shortlists/${r.profileId}`)
      setResults((list) =>
        list.map((x) => (x.profileId === r.profileId ? { ...x, shortlisted: !x.shortlisted } : x))
      )
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const handleSent = (profileId) => {
    setSent((s) => [...s, profileId])
    setNotice('Interest sent.')
    setTimeout(() => setNotice(''), 3000)
  }

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE))

  return (
    <div className="grid gap-6 lg:grid-cols-[250px_1fr]">
      {/* Filters */}
      <form onSubmit={applyFilters} className="card h-fit p-5">
        <h2 className="mb-4 text-lg font-bold">Filters</h2>

        <label className="label">Age</label>
        <div className="mb-4 grid grid-cols-2 gap-2">
          <input type="number" min="18" max="80" className="input" placeholder="From"
            value={draft.minAge} onChange={set('minAge')} />
          <input type="number" min="18" max="80" className="input" placeholder="To"
            value={draft.maxAge} onChange={set('maxAge')} />
        </div>

        <label className="label">Religion</label>
        <select className="input mb-4" value={draft.religion} onChange={set('religion')}>
          <option value="">Any</option>
          {RELIGIONS.map((r) => (
            <option key={r} value={r}>{r}</option>
          ))}
        </select>

        <p className="label">Minimum trust level</p>
        <div className="mb-5 space-y-2 text-sm">
          {[
            ['', 'Any level'],
            ['2', 'Level 2 and above'],
            ['3', 'Level 3 only'],
          ].map(([value, label]) => (
            <label key={label} className="flex items-center gap-2">
              <input type="radio" name="trust" className="accent-brand"
                checked={draft.minTrustLevel === value}
                onChange={() => setDraft({ ...draft, minTrustLevel: value })} />
              {label}
            </label>
          ))}
        </div>

        <button type="submit" className="btn-primary">Apply filters</button>
        <button type="button" onClick={resetFilters} className="btn-outline mt-2 w-full">
          Reset
        </button>
      </form>

      {/* Results */}
      <div>
        <form onSubmit={applyFilters} className="mb-4">
          <input
            className="input rounded-full"
            placeholder="Search by city"
            value={draft.city}
            onChange={set('city')}
          />
        </form>

        {notice && (
          <p className="mb-3 rounded-lg bg-green-50 px-3 py-2 text-sm text-ok">{notice}</p>
        )}
        {error && (
          <p className="mb-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
        )}

        <p className="mb-3 text-xs text-muted">{loading ? 'Searching...' : `${total} profiles found`}</p>

        {!loading && results.length === 0 && !error && (
          <div className="card p-10 text-center">
            <h2 className="text-xl font-bold">No profiles found</h2>
            <p className="mt-1 text-sm text-muted">Try fewer filters or a different city.</p>
            <button onClick={resetFilters} className="btn-outline mt-4">Reset filters</button>
          </div>
        )}

        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {results.map((r) => {
            const active = activeLabel(r.lastActiveAt)
            const alreadySent = sent.includes(r.profileId)
            return (
              <div key={r.profileId} className="card p-3">
                <div className="relative mb-3 h-40 overflow-hidden rounded-xl bg-brand-soft">
                  {r.primaryPhotoUrl ? (
                    <img src={r.primaryPhotoUrl} alt={r.fullName} className="h-full w-full object-cover" />
                  ) : (
                    <div className="flex h-full items-end justify-center">
                      <div className="h-24 w-24 rounded-t-full bg-[#dcc8cd]" />
                    </div>
                  )}
                  <div className="absolute left-2 top-2">
                    <TrustBadge level={r.trustLevel} />
                  </div>
                  <button
                    onClick={() => toggleShortlist(r)}
                    aria-label="Shortlist"
                    className="absolute right-2 top-2 flex h-7 w-7 items-center justify-center rounded-full bg-white text-sm shadow"
                  >
                    <span className={r.shortlisted ? 'text-brand' : 'text-muted'}>
                      {r.shortlisted ? '♥' : '♡'}
                    </span>
                  </button>
                </div>

                <p className="font-semibold">{r.fullName}</p>
                <p className="text-xs text-muted">
                  {r.age} yrs | {r.city || 'City not set'}
                </p>
                <p className="mb-3 mt-1 flex items-center gap-1 text-xs text-muted">
                  <span className={`inline-block h-2 w-2 rounded-full ${active.fresh ? 'bg-ok' : 'bg-warn'}`} />
                  {active.text}
                </p>

                <div className="grid grid-cols-2 gap-2">
                  <Link to={`/app/profiles/${r.profileId}`} className="btn-outline text-center">
                    View
                  </Link>
                  <button
                    disabled={alreadySent}
                    onClick={() => setTarget(r)}
                    className="btn-primary"
                  >
                    {alreadySent ? 'Sent' : 'Send Interest'}
                  </button>
                </div>
              </div>
            )
          })}
        </div>

        {total > PAGE_SIZE && (
          <div className="mt-6 flex items-center justify-center gap-4 text-sm">
            <button disabled={page === 0} onClick={() => setPage(page - 1)} className="btn-outline">
              Previous
            </button>
            <span className="text-muted">Page {page + 1} of {totalPages}</span>
            <button disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)} className="btn-outline">
              Next
            </button>
          </div>
        )}
      </div>

      {target && (
        <InterestModal target={target} onClose={() => setTarget(null)} onSent={handleSent} />
      )}
    </div>
  )
}