import { useEffect, useState, useCallback } from 'react'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'
import { timeAgo } from '../utils/time'

export default function AdminAuditLogs() {
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadLogs = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/api/admin/audit-logs?limit=50')
      setLogs(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadLogs()
  }, [loadLogs])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-ink">Platform Audit Log</h1>
        <p className="mt-1 text-sm text-muted">Immutable record of security and administrative operations</p>
      </div>

      {error && (
        <p className="rounded-lg bg-red-50 p-3 text-sm text-red-600">{error}</p>
      )}

      <div className="card overflow-hidden">
        {loading ? (
          <p className="p-8 text-center text-sm text-muted">Loading audit records...</p>
        ) : logs.length === 0 ? (
          <div className="p-12 text-center text-sm text-muted">
            No audit events recorded yet.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-surface text-xs font-semibold text-muted border-b border-line">
                <tr>
                  <th className="px-6 py-3">ACTION</th>
                  <th className="px-6 py-3">ENTITY</th>
                  <th className="px-6 py-3">DETAILS</th>
                  <th className="px-6 py-3">ACTOR USER ID</th>
                  <th className="px-6 py-3 text-right">TIMESTAMP</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {logs.map((l) => (
                  <tr key={l.id} className="hover:bg-surface/50">
                    <td className="px-6 py-4 font-mono font-semibold text-xs text-brand">
                      {l.action}
                    </td>
                    <td className="px-6 py-4 text-xs text-ink">
                      {l.entityType} {l.entityId ? `#${l.entityId}` : ''}
                    </td>
                    <td className="px-6 py-4 text-xs text-muted max-w-sm truncate">
                      {l.details || '—'}
                    </td>
                    <td className="px-6 py-4 text-xs font-mono text-muted">
                      {l.actorUserId || 'System'}
                    </td>
                    <td className="px-6 py-4 text-xs text-muted text-right">
                      {timeAgo(l.createdAt)}
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

