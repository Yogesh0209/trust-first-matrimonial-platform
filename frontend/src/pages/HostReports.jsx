import { useEffect, useState, useCallback } from 'react'
import api from '../api/axios'
import TrustBadge from '../components/TrustBadge'
import { getErrorMessage } from '../utils/errors'
import { timeAgo } from '../utils/time'

function formatReason(r) {
  if (!r) return 'Report'
  return r
    .split('_')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join(' ')
}

export default function HostReports() {
  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selectedReport, setSelectedReport] = useState(null)
  const [notes, setNotes] = useState('')
  const [resolving, setResolving] = useState(false)
  const [actionMessage, setActionMessage] = useState('')

  const loadReports = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/api/host/reports')
      const list = Array.isArray(res.data) ? res.data : []
      setReports(list)
      if (list.length > 0 && !selectedReport) {
        setSelectedReport(list[0])
      }
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [selectedReport])

  useEffect(() => {
    loadReports()
  }, [loadReports])

  const handleResolve = async (status) => {
    if (!selectedReport) return
    setResolving(true)
    setError('')
    setActionMessage('')
    try {
      await api.put(`/api/host/reports/${selectedReport.id}/resolve`, {
        status,
        notes: notes.trim() || undefined,
      })
      setActionMessage(`Report marked as ${status.toLowerCase()}.`)
      setNotes('')
      // Update local report status or remove resolved
      const updated = reports.map((r) =>
        r.id === selectedReport.id ? { ...r, status } : r
      )
      setReports(updated)
      setSelectedReport((prev) => (prev ? { ...prev, status } : null))
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setResolving(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-ink">Reported profiles</h1>
        <p className="mt-1 text-sm text-muted">Profiles reported by members for safety concerns</p>
      </div>

      {error && (
        <p className="rounded-lg bg-red-50 p-3 text-sm text-red-600">{error}</p>
      )}

      {actionMessage && (
        <p className="rounded-lg bg-green-50 p-3 text-sm text-ok">{actionMessage}</p>
      )}

      {/* Reports Table */}
      <div className="card overflow-hidden">
        {loading ? (
          <p className="p-8 text-center text-sm text-muted">Loading reported profiles...</p>
        ) : reports.length === 0 ? (
          <div className="p-12 text-center text-sm text-muted">
            No reported profiles found. Your bureau is clean!
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-surface text-xs font-semibold text-muted border-b border-line">
                <tr>
                  <th className="px-6 py-3">PROFILE</th>
                  <th className="px-6 py-3">REASON</th>
                  <th className="px-6 py-3">REPORTED BY</th>
                  <th className="px-6 py-3">DATE</th>
                  <th className="px-6 py-3">STATUS</th>
                  <th className="px-6 py-3 text-right">ACTION</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {reports.map((r) => {
                  const isSelected = selectedReport?.id === r.id
                  return (
                    <tr
                      key={r.id}
                      onClick={() => setSelectedReport(r)}
                      className={`cursor-pointer transition ${
                        isSelected ? 'bg-brand-soft/40 font-medium' : 'hover:bg-surface/50'
                      }`}
                    >
                      <td className="px-6 py-3.5 font-semibold text-ink">
                        {r.reportedName || `Profile #${r.reportedProfileId}`}
                      </td>
                      <td className="px-6 py-3.5 text-xs text-muted">
                        <span className="rounded-md bg-amber-50 px-2 py-0.5 text-warn font-semibold">
                          {formatReason(r.reason)}
                        </span>
                      </td>
                      <td className="px-6 py-3.5 text-muted">
                        {r.reporterName || `User #${r.reporterProfileId}`}
                      </td>
                      <td className="px-6 py-3.5 text-xs text-muted">
                        {timeAgo(r.createdAt)}
                      </td>
                      <td className="px-6 py-3.5 text-xs">
                        <span
                          className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${
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
                      <td className="px-6 py-3.5 text-right">
                        <button
                          type="button"
                          className={isSelected ? 'btn-primary text-xs' : 'btn-outline text-xs'}
                          style={{ width: 'auto', paddingBlock: 4, paddingInline: 12 }}
                        >
                          Review
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Selected Report Detail Card (Design 11) */}
      {selectedReport && (
        <div className="card p-6 sm:p-8 space-y-6">
          <div className="border-b border-line pb-4 flex items-center justify-between">
            <h2 className="text-lg font-bold text-ink">Selected report</h2>
            <span className="text-xs text-muted">ID: #{selectedReport.id}</span>
          </div>

          <div className="flex flex-wrap items-start justify-between gap-4">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-full bg-brand-soft text-brand font-bold text-lg">
                {(selectedReport.reportedName || 'U').charAt(0).toUpperCase()}
              </div>
              <div>
                <h3 className="text-base font-bold text-ink">
                  {selectedReport.reportedName || `Profile #${selectedReport.reportedProfileId}`}
                </h3>
                <div className="mt-1 flex items-center gap-2">
                  <TrustBadge level={2} />
                  <span className="rounded-full bg-amber-50 px-2.5 py-0.5 text-xs font-semibold text-warn">
                    {formatReason(selectedReport.reason)}
                  </span>
                </div>
              </div>
            </div>

            <div className="text-right text-xs text-muted">
              Reported by <span className="font-semibold text-ink">{selectedReport.reporterName}</span> • {timeAgo(selectedReport.createdAt)}
            </div>
          </div>

          {selectedReport.details && (
            <div className="rounded-xl border border-line bg-surface p-4">
              <p className="text-xs font-semibold text-muted mb-1">Report details:</p>
              <p className="text-sm italic text-ink leading-relaxed">
                &ldquo;{selectedReport.details}&rdquo;
              </p>
            </div>
          )}

          {/* Notes input */}
          <div>
            <label className="label">Resolution notes (optional)</label>
            <textarea
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Add optional notes about the decision taken..."
              className="input resize-none text-xs"
            />
          </div>

          {/* Decision Buttons */}
          <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
            <div className="flex gap-3">
              <button
                type="button"
                onClick={() => handleResolve('WARNED')}
                disabled={resolving}
                className="btn-outline text-xs"
                style={{ paddingBlock: 8, paddingInline: 18 }}
              >
                Warn user
              </button>
              <button
                type="button"
                onClick={() => handleResolve('REMOVED')}
                disabled={resolving}
                className="rounded-lg border border-red-300 bg-red-50 px-4 py-2 text-xs font-semibold text-red-600 hover:bg-red-100"
              >
                Remove profile
              </button>
            </div>

            <button
              type="button"
              onClick={() => handleResolve('DISMISSED')}
              disabled={resolving}
              className="btn-primary"
              style={{ width: 'auto', paddingBlock: 8, paddingInline: 24 }}
            >
              Dismiss
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

