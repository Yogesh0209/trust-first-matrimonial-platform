import { useEffect, useState, useMemo } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'
import { timeAgo } from '../utils/time'

export default function HostDashboard() {
  const navigate = useNavigate()
  const [stats, setStats] = useState(null)
  const [members, setMembers] = useState([])
  const [verifications, setVerifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [now] = useState(() => Date.now())

  useEffect(() => {
    Promise.allSettled([
      api.get('/api/host/dashboard'),
      api.get('/api/host/members'),
      api.get('/api/host/verifications'),
    ])
      .then(([dashRes, memRes, verRes]) => {
        if (dashRes.status === 'fulfilled') setStats(dashRes.value.data)
        else setError(getErrorMessage(dashRes.reason))

        if (memRes.status === 'fulfilled') setMembers(Array.isArray(memRes.value.data) ? memRes.value.data : [])
        if (verRes.status === 'fulfilled') setVerifications(Array.isArray(verRes.value.data) ? verRes.value.data : [])
      })
      .finally(() => setLoading(false))
  }, [])

  // Calculate analytics with useMemo
  const { trustCounts, maxTrust, monthLabels, monthCounts, maxMonthCount, joinedThisMonth } = useMemo(() => {
    const counts = { 1: 0, 2: 0, 3: 0 }
    members.forEach((m) => {
      const lvl = m.trustLevel || 1
      if (counts[lvl] !== undefined) counts[lvl] += 1
    })
    const maxT = Math.max(counts[1], counts[2], counts[3], 1)

    const nowDate = new Date(now)
    const labels = []
    const mCounts = []
    for (let i = 5; i >= 0; i--) {
      const d = new Date(nowDate.getFullYear(), nowDate.getMonth() - i, 1)
      const label = d.toLocaleString('en-US', { month: 'short' })
      labels.push(label)

      const count = members.filter((m) => {
        if (!m.createdAt) return false
        const mDate = new Date(m.createdAt)
        return mDate.getMonth() === d.getMonth() && mDate.getFullYear() === d.getFullYear()
      }).length
      mCounts.push(count)
    }
    const maxM = Math.max(...mCounts, 5)

    const thirtyDaysAgo = now - 30 * 24 * 60 * 60 * 1000
    const joinedRecent = members.filter((m) => m.createdAt && new Date(m.createdAt).getTime() > thirtyDaysAgo).length

    return {
      trustCounts: counts,
      maxTrust: maxT,
      monthLabels: labels,
      monthCounts: mCounts,
      maxMonthCount: maxM,
      joinedThisMonth: joinedRecent,
    }
  }, [members, now])

  if (loading) {
    return <p className="text-sm text-muted">Loading host dashboard...</p>
  }

  // Quota percentage
  const quotaPercent = stats?.quotaUsagePercentage != null
    ? Math.min(100, Math.round(stats.quotaUsagePercentage))
    : stats?.memberLimit
    ? Math.min(100, Math.round(((stats.totalMembers || 0) / stats.memberLimit) * 100))
    : null

  // First 5 pending verifications
  const pendingFive = verifications.slice(0, 5)

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-ink">Dashboard</h1>
        <p className="mt-1 text-sm text-muted">Overview of your marriage bureau</p>
      </div>

      {error && (
        <p className="rounded-lg bg-red-50 p-3 text-sm text-red-600">{error}</p>
      )}

      {/* Stats Cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="card p-5">
          <p className="text-xs font-semibold text-muted">Total members</p>
          <div className="mt-3 flex items-baseline justify-between">
            <span className="font-serif text-3xl font-bold text-ink">
              {stats?.totalMembers ?? members.length}
            </span>
            {joinedThisMonth > 0 && (
              <span className="text-xs font-semibold text-ok">+{joinedThisMonth} this month</span>
            )}
          </div>
          {quotaPercent !== null && (
            <div className="mt-3">
              <div className="flex justify-between text-[11px] text-muted mb-1">
                <span>Quota used</span>
                <span>{quotaPercent}% ({stats?.totalMembers || 0}/{stats?.memberLimit || '∞'})</span>
              </div>
              <div className="h-1.5 w-full rounded-full bg-sand">
                <div
                  className="h-1.5 rounded-full bg-brand"
                  style={{ width: `${quotaPercent}%` }}
                />
              </div>
            </div>
          )}
        </div>

        <div className="card p-5">
          <p className="text-xs font-semibold text-muted">Pending verifications</p>
          <div className="mt-3">
            <span className="font-serif text-3xl font-bold text-ink">
              {stats?.pendingVerifications ?? verifications.length}
            </span>
          </div>
          <p className="mt-3 text-xs text-muted">Awaiting review</p>
        </div>

        <div className="card p-5">
          <p className="text-xs font-semibold text-muted">Open reports</p>
          <div className="mt-3">
            <span className="font-serif text-3xl font-bold text-ink">
              {stats?.openReports ?? 0}
            </span>
          </div>
          <p className="mt-3 text-xs text-muted">Safety alerts</p>
        </div>

        <div className="card p-5">
          <p className="text-xs font-semibold text-muted">Active members</p>
          <div className="mt-3 flex items-baseline justify-between">
            <span className="font-serif text-3xl font-bold text-ink">
              {stats?.activeMembers ?? members.filter((m) => m.status === 'ACTIVE').length}
            </span>
            <span className="text-xs font-semibold text-muted">
              {members.length > 0
                ? `${Math.round(((stats?.activeMembers ?? members.filter((m) => m.status === 'ACTIVE').length) / members.length) * 100)}%`
                : ''}
            </span>
          </div>
          <p className="mt-3 text-xs text-muted">Visible in search</p>
        </div>
      </div>

      {/* Visual Analytics Grid */}
      <div className="grid gap-6 lg:grid-cols-3">
        {/* Joined per month bar chart */}
        <div className="card p-6 lg:col-span-2">
          <h2 className="text-base font-bold text-ink">Members joined per month</h2>
          <div className="mt-6 flex h-48 items-end gap-4 sm:gap-8 pt-6 border-b border-line">
            {monthLabels.map((lbl, idx) => {
              const val = monthCounts[idx]
              const heightPercent = Math.max(8, Math.round((val / maxMonthCount) * 100))
              const isCurrent = idx === monthLabels.length - 1
              return (
                <div key={lbl} className="flex flex-1 flex-col items-center gap-2 h-full justify-end">
                  <span className="text-[11px] font-semibold text-muted">{val}</span>
                  <div
                    className={`w-full max-w-[40px] rounded-t-md transition-all ${
                      isCurrent ? 'bg-brand' : 'bg-brand-soft hover:bg-brand/40'
                    }`}
                    style={{ height: `${heightPercent}%` }}
                  />
                  <span className="text-xs text-muted">{lbl}</span>
                </div>
              )
            })}
          </div>
        </div>

        {/* Members by trust level */}
        <div className="card p-6 flex flex-col justify-between">
          <h2 className="text-base font-bold text-ink">Members by trust level</h2>
          <div className="mt-6 space-y-4">
            {[1, 2, 3].map((lvl) => {
              const cnt = trustCounts[lvl]
              const pct = Math.round((cnt / maxTrust) * 100)
              return (
                <div key={lvl}>
                  <div className="flex justify-between text-xs font-semibold">
                    <span className="text-muted">Level {lvl}</span>
                    <span className="text-ink">{cnt}</span>
                  </div>
                  <div className="mt-1.5 h-2 w-full rounded-full bg-sand">
                    <div
                      className="h-2 rounded-full bg-brand"
                      style={{ width: `${Math.max(4, pct)}%` }}
                    />
                  </div>
                </div>
              )
            })}
          </div>
          <p className="mt-6 text-[11px] text-muted">
            Level 1: Phone/Email | Level 2: Selfie | Level 3: Government ID
          </p>
        </div>
      </div>

      {/* Pending Verifications Preview */}
      <div className="card overflow-hidden">
        <div className="flex items-center justify-between border-b border-line px-6 py-4">
          <h2 className="text-base font-bold text-ink">Pending verifications</h2>
          <Link
            to="/host/members?tab=verifications"
            className="text-xs font-semibold text-brand hover:underline"
          >
            View all
          </Link>
        </div>

        {pendingFive.length === 0 ? (
          <p className="p-8 text-center text-sm text-muted">
            No pending verifications at this time.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-surface text-xs text-muted border-b border-line">
                <tr>
                  <th className="px-6 py-3 font-semibold">NAME / PROFILE</th>
                  <th className="px-6 py-3 font-semibold">REQUEST</th>
                  <th className="px-6 py-3 font-semibold">SUBMITTED</th>
                  <th className="px-6 py-3 font-semibold text-right">ACTION</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {pendingFive.map((v) => (
                  <tr key={v.id} className="hover:bg-surface/50">
                    <td className="px-6 py-3 font-medium text-ink">
                      {v.fullName || `Profile #${v.profileId}`}
                    </td>
                    <td className="px-6 py-3 text-muted">
                      {v.type === 'GOV_ID' ? 'Government ID' : 'Selfie'}
                    </td>
                    <td className="px-6 py-3 text-xs text-muted">
                      {timeAgo(v.submittedAt)}
                    </td>
                    <td className="px-6 py-3 text-right">
                      <button
                        type="button"
                        onClick={() => navigate('/host/members?tab=verifications')}
                        className="btn-outline text-xs"
                        style={{ paddingBlock: 4, paddingInline: 12 }}
                      >
                        Review
                      </button>
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
