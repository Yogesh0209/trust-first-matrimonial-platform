import { useEffect, useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'
import { timeAgo } from '../utils/time'

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [tenants, setTenants] = useState([])
  const [schedulerRuns, setSchedulerRuns] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Scheduler job running state
  const [runningJob, setRunningJob] = useState('')
  const [jobResult, setJobResult] = useState('')

  const loadData = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [dashRes, tenantsRes, schedRes] = await Promise.allSettled([
        api.get('/api/admin/dashboard'),
        api.get('/api/admin/tenants'),
        api.get('/api/admin/scheduler-runs?limit=20'),
      ])

      if (dashRes.status === 'fulfilled') setStats(dashRes.value.data)
      else setError(getErrorMessage(dashRes.reason))

      if (tenantsRes.status === 'fulfilled') {
        setTenants(Array.isArray(tenantsRes.value.data) ? tenantsRes.value.data : [])
      }
      if (schedRes.status === 'fulfilled') {
        setSchedulerRuns(Array.isArray(schedRes.value.data) ? schedRes.value.data : [])
      }
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadData()
  }, [loadData])

  // Run freshness (inactivity check)
  const handleRunFreshness = async () => {
    setRunningJob('freshness')
    setJobResult('')
    setError('')
    try {
      const res = await api.post('/api/admin/scheduler-runs/freshness')
      setJobResult(`Freshness check finished: ${JSON.stringify(res.data || 'Completed successfully')}`)
      loadData()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setRunningJob('')
    }
  }

  // Run subscription expiry check
  const handleRunExpiry = async () => {
    setRunningJob('expiry')
    setJobResult('')
    setError('')
    try {
      const res = await api.post('/api/admin/subscriptions/run-expiry-check')
      const msg = res.data
        ? `Expiry check finished: ${res.data.expired || 0} expired, ${res.data.warned || 0} warned.`
        : 'Subscription expiry check completed.'
      setJobResult(msg)
      loadData()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setRunningJob('')
    }
  }

  if (loading) {
    return <p className="text-sm text-muted">Loading platform overview...</p>
  }

  // Plan distribution entries
  const planDist = stats?.planDistribution || {}
  const planEntries = Object.entries(planDist)
  const maxPlanCount = Math.max(...Object.values(planDist).map(Number), 1)

  // Recent 5 bureaus
  const recentHosts = tenants.slice(0, 5)

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-ink">Platform overview</h1>
        <p className="mt-1 text-sm text-muted">Global operations, hosts, and automated tasks</p>
      </div>

      {error && (
        <p className="rounded-lg bg-red-50 p-3 text-sm text-red-600">{error}</p>
      )}

      {jobResult && (
        <p className="rounded-lg bg-green-50 p-3 text-sm text-ok font-medium">{jobResult}</p>
      )}

      {/* Stats Cards (Design 13) */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="card p-5">
          <p className="text-xs font-semibold text-muted">Total hosts / bureaus</p>
          <div className="mt-3 flex items-baseline justify-between">
            <span className="font-serif text-3xl font-bold text-ink">
              {stats?.totalBureaus ?? tenants.length}
            </span>
            {stats?.pendingBureaus > 0 && (
              <span className="text-xs font-semibold text-warn">
                {stats.pendingBureaus} pending
              </span>
            )}
          </div>
          <p className="mt-3 text-xs text-muted">Active: {stats?.activeBureaus ?? 0}</p>
        </div>

        <div className="card p-5">
          <p className="text-xs font-semibold text-muted">Platform candidates</p>
          <div className="mt-3">
            <span className="font-serif text-3xl font-bold text-ink">
              {stats?.totalPlatformUsers ?? 0}
            </span>
          </div>
          <p className="mt-3 text-xs text-muted">Across all registered bureaus</p>
        </div>

        <div className="card p-5">
          <p className="text-xs font-semibold text-muted">Verifications reviewed</p>
          <div className="mt-3">
            <span className="font-serif text-3xl font-bold text-ink">
              {stats?.totalVerificationsReviewed ?? 0}
            </span>
          </div>
          <p className="mt-3 text-xs text-muted">Selfie & Government IDs</p>
        </div>

        <div className="card p-5">
          <p className="text-xs font-semibold text-muted">Open safety reports</p>
          <div className="mt-3 flex items-baseline justify-between">
            <span className="font-serif text-3xl font-bold text-ink">
              {stats?.openReports ?? 0}
            </span>
            {stats?.openReports > 0 && (
              <Link to="/admin/reports" className="text-xs font-semibold text-brand hover:underline">
                Review
              </Link>
            )}
          </div>
          <p className="mt-3 text-xs text-muted">Platform-wide flags</p>
        </div>
      </div>

      {/* Plan Distribution & Scheduler Grid */}
      <div className="grid gap-6 lg:grid-cols-2 items-start">
        {/* Plan Distribution */}
        <div className="card p-6">
          <h2 className="text-base font-bold text-ink">Host Plan Distribution</h2>
          <p className="text-xs text-muted mt-0.5">Active subscriptions across bureau tiers</p>

          <div className="mt-6 space-y-4">
            {planEntries.length === 0 ? (
              <p className="text-xs text-muted">No plan distribution data available.</p>
            ) : (
              planEntries.map(([name, count]) => {
                const pct = Math.round((Number(count) / maxPlanCount) * 100)
                return (
                  <div key={name}>
                    <div className="flex justify-between text-xs font-semibold">
                      <span className="text-ink">{name}</span>
                      <span className="text-muted">{count} bureaus</span>
                    </div>
                    <div className="mt-1.5 h-2 w-full rounded-full bg-sand">
                      <div
                        className="h-2 rounded-full bg-brand"
                        style={{ width: `${Math.max(6, pct)}%` }}
                      />
                    </div>
                  </div>
                )
              })
            )}
          </div>
        </div>

        {/* Scheduler & Automation Controls */}
        <div className="card p-6 space-y-5">
          <div>
            <h2 className="text-base font-bold text-ink">Scheduled Maintenance Tasks</h2>
            <p className="text-xs text-muted mt-0.5">Trigger background maintenance jobs on demand</p>
          </div>

          <div className="flex flex-col sm:flex-row gap-3">
            <button
              type="button"
              onClick={handleRunFreshness}
              disabled={Boolean(runningJob)}
              className="btn-outline flex-1 text-xs"
              style={{ paddingBlock: 10 }}
            >
              {runningJob === 'freshness' ? 'Running check...' : 'Run Inactive Profile Check'}
            </button>
            <button
              type="button"
              onClick={handleRunExpiry}
              disabled={Boolean(runningJob)}
              className="btn-primary flex-1 text-xs"
              style={{ paddingBlock: 10 }}
            >
              {runningJob === 'expiry' ? 'Running check...' : 'Run Subscription Expiry Check'}
            </button>
          </div>

          {/* Recent scheduler runs list */}
          <div className="pt-2">
            <p className="text-xs font-semibold text-muted mb-2">Recent Job Runs</p>
            {schedulerRuns.length === 0 ? (
              <p className="text-xs text-muted">No recent job logs found.</p>
            ) : (
              <div className="max-h-40 overflow-y-auto divide-y divide-line rounded-lg border border-line bg-surface text-xs">
                {schedulerRuns.slice(0, 5).map((run) => (
                  <div key={run.id} className="p-2.5 flex justify-between">
                    <div>
                      <span className="font-semibold text-ink">{run.action || 'Job'}</span>
                      {run.details && <p className="text-muted text-[11px] mt-0.5">{run.details}</p>}
                    </div>
                    <span className="text-muted text-[11px] shrink-0">{timeAgo(run.createdAt)}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Recently Joined Hosts (Design 13) */}
      <div className="card overflow-hidden">
        <div className="flex items-center justify-between border-b border-line px-6 py-4">
          <h2 className="text-base font-bold text-ink">Recently joined hosts</h2>
          <Link to="/admin/hosts" className="text-xs font-semibold text-brand hover:underline">
            View all hosts
          </Link>
        </div>

        {recentHosts.length === 0 ? (
          <p className="p-8 text-center text-sm text-muted">No hosts registered yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-surface text-xs font-semibold text-muted border-b border-line">
                <tr>
                  <th className="px-6 py-3">BUREAU</th>
                  <th className="px-6 py-3">CITY</th>
                  <th className="px-6 py-3">PLAN</th>
                  <th className="px-6 py-3">STATUS</th>
                  <th className="px-6 py-3">JOINED</th>
                  <th className="px-6 py-3 text-right">ACTION</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {recentHosts.map((h) => (
                  <tr key={h.id} className="hover:bg-surface/50">
                    <td className="px-6 py-3.5">
                      <p className="font-semibold text-ink">{h.name}</p>
                      <p className="text-xs font-mono text-muted">{h.bureauCode}</p>
                    </td>
                    <td className="px-6 py-3.5 text-muted">{h.city || '—'}</td>
                    <td className="px-6 py-3.5 text-xs text-ink font-medium">
                      {h.activePlan || 'Basic'}
                    </td>
                    <td className="px-6 py-3.5">
                      <span
                        className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                          h.status === 'ACTIVE'
                            ? 'bg-green-50 text-ok'
                            : h.status === 'PENDING'
                            ? 'bg-amber-50 text-warn'
                            : 'bg-red-50 text-red-600'
                        }`}
                      >
                        {h.status}
                      </span>
                    </td>
                    <td className="px-6 py-3.5 text-xs text-muted">
                      {timeAgo(h.createdAt)}
                    </td>
                    <td className="px-6 py-3.5 text-right">
                      <Link
                        to={`/admin/hosts?selected=${h.id}`}
                        className="btn-outline text-xs"
                        style={{ paddingBlock: 4, paddingInline: 12 }}
                      >
                        Manage
                      </Link>
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

