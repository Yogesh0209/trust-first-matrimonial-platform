import { useEffect, useState, useCallback, useMemo } from 'react'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'
import { timeAgo } from '../utils/time'

function formatReason(r) {
  if (!r) return 'Report'
  return r
    .split('_')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join(' ')
}

export default function AdminReports() {
  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filter, setFilter] = useState('ALL')

  const loadReports = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/api/admin/reports')
      setReports(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadReports()
  }, [loadReports])

  const filtered = useMemo(() => {
    if (filter === 'ALL') return reports
    return reports.filter((r) => r.status === filter)
  }, [reports, filter])

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-ink">Platform Reports</h1>
          <p className="mt-1 text-sm text-muted">All safety and profile reports across marriage bureaus</p>
        </div>

        <div className="flex gap-1.5 overflow-x-auto">
          {['ALL', 'OPEN', 'WARNED', 'REMOVED', 'DISMISSED'].map((st) => (
            <button
              key={st}
              type="button"
              onClick={() => setFilter(st)}
              className={`rounded-full px-3 py-1 text-xs font-semibold transition ${
                filter === st
                  ? 'bg-brand text-white'
                  : 'bg-sand text-muted hover:text-ink'
              }`}
            >
              {st}
            </button>
          ))}
        </div>
      </div>

      {error && (
        <p className="rounded-lg bg-red-50 p-3 text-sm text-red-600">{error}</p>
      )}

      <div className="card overflow-hidden">
        {loading ? (
          <p className="p-8 text-center text-sm text-muted">Loading reports...</p>
        ) : filtered.length === 0 ? (
          <div className="p-12 text-center text-sm text-muted">
            No platform reports matching the selected status.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-surface text-xs font-semibold text-muted border-b border-line">
                <tr>
                  <th className="px-6 py-3">REPORTED PROFILE</th>
                  <th className="px-6 py-3">REASON</th>
                  <th className="px-6 py-3">REPORTED BY</th>
                  <th className="px-6 py-3">DETAILS</th>
                  <th className="px-6 py-3">STATUS</th>
                  <th className="px-6 py-3 text-right">DATE</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {filtered.map((r) => (
                  <tr key={r.id} className="hover:bg-surface/50">
                    <td className="px-6 py-4 font-semibold text-ink">
                      {r.reportedName || `Profile #${r.reportedProfileId}`}
                    </td>
                    <td className="px-6 py-4 text-xs">
                      <span className="rounded-md bg-amber-50 px-2 py-0.5 text-warn font-semibold">
                        {formatReason(r.reason)}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-xs text-muted">
                      {r.reporterName || `User #${r.reporterProfileId}`}
                    </td>
                    <td className="px-6 py-4 text-xs text-ink max-w-xs truncate">
                      {r.details || '—'}
                    </td>
                    <td className="px-6 py-4 text-xs">
                      <span
                        className={`rounded-full px-2.5 py-0.5 font-semibold ${
                          r.status === 'OPEN'
                            ? 'bg-amber-50 text-warn'
                            : r.status === 'REMOVED'
                            ? 'bg-red-50 text-red-600'
                            : 'bg-sand text-muted'
                        }`}
                      >
                        {r.status || 'OPEN'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-xs text-muted text-right">
                      {timeAgo(r.createdAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

