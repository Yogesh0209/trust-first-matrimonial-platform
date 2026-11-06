import { useEffect, useState, useCallback, useMemo } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'

export default function AdminHosts() {
  const [searchParams] = useSearchParams()
  const initialSelectedId = searchParams.get('selected')

  const [tenants, setTenants] = useState([])
  const [plans, setPlans] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Search and status filter
  const [searchQuery, setSearchQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')

  // Manage Side Panel / Modal state
  const [selectedBureau, setSelectedBureau] = useState(null)
  const [actionSuccess, setActionSuccess] = useState('')
  const [actionError, setActionError] = useState('')
  const [busyAction, setBusyAction] = useState(false)

  // Status change reason
  const [suspendReason, setSuspendReason] = useState('')
  const [showSuspendInput, setShowSuspendInput] = useState(false)

  // Subscription assign form
  const [assignPlanId, setAssignPlanId] = useState('')
  const [assignMonths, setAssignMonths] = useState(12)

  const loadData = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [tenantsRes, plansRes] = await Promise.all([
        api.get('/api/admin/tenants'),
        api.get('/api/admin/plans').catch(() => ({ data: [] })),
      ])
      const list = Array.isArray(tenantsRes.data) ? tenantsRes.data : []
      setTenants(list)
      const pList = Array.isArray(plansRes.data) ? plansRes.data : []
      setPlans(pList)
      if (pList.length > 0 && !assignPlanId) {
        setAssignPlanId(pList[0].id)
      }

      if (initialSelectedId) {
        const found = list.find((t) => String(t.id) === String(initialSelectedId))
        if (found) setSelectedBureau(found)
      }
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [initialSelectedId, assignPlanId])

  useEffect(() => {
    loadData()
  }, [loadData])

  // Filtered tenants list
  const filteredTenants = useMemo(() => {
    return tenants.filter((t) => {
      const query = searchQuery.trim().toLowerCase()
      const matchesSearch =
        !query ||
        (t.name && t.name.toLowerCase().includes(query)) ||
        (t.city && t.city.toLowerCase().includes(query)) ||
        (t.bureauCode && t.bureauCode.toLowerCase().includes(query))

      const matchesStatus = statusFilter === 'ALL' || t.status === statusFilter
      return matchesSearch && matchesStatus
    })
  }, [tenants, searchQuery, statusFilter])

  // Status update handler (APPROVE / SUSPEND / REACTIVATE)
  const handleUpdateStatus = async (newStatus) => {
    if (!selectedBureau) return
    if (newStatus === 'SUSPENDED' && !suspendReason.trim()) {
      setActionError('A reason is required to suspend a bureau.')
      return
    }

    setBusyAction(true)
    setActionError('')
    setActionSuccess('')
    try {
      await api.put(`/api/admin/tenants/${selectedBureau.id}/status`, {
        status: newStatus,
        reason: newStatus === 'SUSPENDED' ? suspendReason.trim() : undefined,
      })
      setActionSuccess(`Bureau status updated to ${newStatus}.`)
      setShowSuspendInput(false)
      setSuspendReason('')

      // Update locally
      setSelectedBureau((prev) => (prev ? { ...prev, status: newStatus } : null))
      setTenants((list) =>
        list.map((t) => (t.id === selectedBureau.id ? { ...t, status: newStatus } : t))
      )
    } catch (err) {
      setActionError(getErrorMessage(err))
    } finally {
      setBusyAction(false)
    }
  }

  // Assign Subscription handler
  const handleAssignSubscription = async (e) => {
    e.preventDefault()
    if (!selectedBureau || !assignPlanId) return

    setBusyAction(true)
    setActionError('')
    setActionSuccess('')
    try {
      await api.post(`/api/admin/tenants/${selectedBureau.id}/subscription`, {
        planId: Number(assignPlanId),
        months: Number(assignMonths),
      })
      const chosenPlan = plans.find((p) => p.id === Number(assignPlanId))
      setActionSuccess(`Assigned ${chosenPlan?.name || 'plan'} for ${assignMonths} months.`)
      // Refresh list
      loadData()
    } catch (err) {
      setActionError(getErrorMessage(err))
    } finally {
      setBusyAction(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-ink">Hosts</h1>
          <p className="mt-1 text-sm text-muted">Marriage bureaus registered on the platform</p>
        </div>

        <Link
          to="/host-register"
          className="btn-primary"
          style={{ width: 'auto', paddingInline: 20 }}
        >
          + Register new host
        </Link>
      </div>

      {error && (
        <p className="rounded-lg bg-red-50 p-3 text-sm text-red-600">{error}</p>
      )}

      {/* Filter and Search Bar (Design 27) */}
      <div className="flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="relative w-full sm:max-w-md">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search bureau name, code or city..."
            className="input"
            style={{ paddingLeft: 38 }}
          />
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            className="absolute left-3 top-3 text-muted"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
        </div>

        {/* Status Filter Buttons */}
        <div className="flex gap-1.5 self-start sm:self-auto overflow-x-auto">
          {['ALL', 'PENDING', 'ACTIVE', 'SUSPENDED'].map((st) => (
            <button
              key={st}
              type="button"
              onClick={() => setStatusFilter(st)}
              className={`rounded-full px-3 py-1 text-xs font-semibold transition ${
                statusFilter === st
                  ? 'bg-brand text-white'
                  : 'bg-sand text-muted hover:text-ink'
              }`}
            >
              {st}
            </button>
          ))}
        </div>
      </div>

      {/* Hosts Table (Design 27) */}
      <div className="card overflow-hidden">
        {loading ? (
          <p className="p-8 text-center text-sm text-muted">Loading registered hosts...</p>
        ) : filteredTenants.length === 0 ? (
          <div className="p-12 text-center text-sm text-muted">
            No marriage bureaus found matching your filter criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-surface text-xs font-semibold text-muted border-b border-line">
                <tr>
                  <th className="px-6 py-3">BUREAU</th>
                  <th className="px-6 py-3">CITY</th>
                  <th className="px-6 py-3">PLAN</th>
                  <th className="px-6 py-3">MEMBERS</th>
                  <th className="px-6 py-3">STATUS</th>
                  <th className="px-6 py-3 text-right">ACTION</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {filteredTenants.map((h) => {
                  const isSelected = selectedBureau?.id === h.id
                  return (
                    <tr
                      key={h.id}
                      className={`transition ${
                        isSelected ? 'bg-brand-soft/40 font-medium' : 'hover:bg-surface/50'
                      }`}
                    >
                      <td className="px-6 py-4">
                        <p className="font-semibold text-ink">{h.name}</p>
                        <p className="text-xs font-mono text-muted">{h.bureauCode}</p>
                      </td>
                      <td className="px-6 py-4 text-muted">{h.city || '—'}</td>
                      <td className="px-6 py-4 text-xs font-medium text-ink">
                        {h.activePlan || 'Basic'}
                      </td>
                      <td className="px-6 py-4 text-muted">
                        {h.memberCount ?? 0}
                      </td>
                      <td className="px-6 py-4">
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
                      <td className="px-6 py-4 text-right">
                        <button
                          type="button"
                          onClick={() => {
                            setSelectedBureau(h)
                            setActionError('')
                            setActionSuccess('')
                            setShowSuspendInput(false)
                          }}
                          className={isSelected ? 'btn-primary text-xs' : 'btn-outline text-xs'}
                          style={{ width: 'auto', paddingBlock: 4, paddingInline: 12 }}
                        >
                          Manage
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

      {/* Manage Bureau Side Panel / Card */}
      {selectedBureau && (
        <div className="card p-6 sm:p-8 space-y-6">
          <div className="flex items-center justify-between border-b border-line pb-4">
            <div>
              <h2 className="text-xl font-bold text-ink">Manage Host: {selectedBureau.name}</h2>
              <p className="text-xs font-mono text-muted">Code: {selectedBureau.bureauCode} • City: {selectedBureau.city || '—'}</p>
            </div>
            <button
              type="button"
              onClick={() => setSelectedBureau(null)}
              className="text-xs text-muted hover:text-ink font-semibold"
            >
              Close panel ✕
            </button>
          </div>

          {actionError && (
            <p className="rounded bg-red-50 p-2.5 text-xs text-red-600">{actionError}</p>
          )}
          {actionSuccess && (
            <p className="rounded bg-green-50 p-2.5 text-xs text-ok">{actionSuccess}</p>
          )}

          <div className="grid gap-6 md:grid-cols-2">
            {/* Status Controls */}
            <div className="rounded-xl border border-line bg-surface p-5 space-y-4">
              <h3 className="text-sm font-bold text-ink">Bureau Status</h3>
              <div className="flex items-center gap-2">
                <span className="text-xs text-muted">Current status:</span>
                <span className="font-semibold text-xs text-ink">{selectedBureau.status}</span>
              </div>

              {selectedBureau.status === 'PENDING' && (
                <div>
                  <p className="text-xs text-muted mb-3">
                    This marriage bureau registered and is awaiting administrative approval.
                  </p>
                  <button
                    type="button"
                    onClick={() => handleUpdateStatus('ACTIVE')}
                    disabled={busyAction}
                    className="btn-primary text-xs"
                    style={{ width: 'auto', paddingInline: 20 }}
                  >
                    {busyAction ? 'Approving...' : 'Approve Bureau'}
                  </button>
                </div>
              )}

              {selectedBureau.status === 'ACTIVE' && (
                <div>
                  {!showSuspendInput ? (
                    <button
                      type="button"
                      onClick={() => setShowSuspendInput(true)}
                      className="rounded border border-red-300 bg-red-50 px-4 py-2 text-xs font-semibold text-red-600 hover:bg-red-100"
                    >
                      Suspend Bureau
                    </button>
                  ) : (
                    <div className="space-y-3 pt-2">
                      <label className="label">Reason for suspension (required)</label>
                      <textarea
                        rows={2}
                        value={suspendReason}
                        onChange={(e) => setSuspendReason(e.target.value)}
                        placeholder="State reason for suspension..."
                        maxLength={500}
                        className="input resize-none text-xs"
                      />
                      <div className="flex gap-2">
                        <button
                          type="button"
                          onClick={() => handleUpdateStatus('SUSPENDED')}
                          disabled={busyAction}
                          className="rounded bg-red-600 px-4 py-1.5 text-xs font-semibold text-white hover:bg-red-700"
                        >
                          Confirm Suspend
                        </button>
                        <button
                          type="button"
                          onClick={() => setShowSuspendInput(false)}
                          className="btn-outline text-xs"
                          style={{ paddingBlock: 4 }}
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {selectedBureau.status === 'SUSPENDED' && (
                <div>
                  <p className="text-xs text-red-600 mb-3">
                    This bureau is currently suspended from the network.
                  </p>
                  <button
                    type="button"
                    onClick={() => handleUpdateStatus('ACTIVE')}
                    disabled={busyAction}
                    className="btn-primary text-xs"
                    style={{ width: 'auto', paddingInline: 20 }}
                  >
                    {busyAction ? 'Reactivating...' : 'Reactivate Bureau'}
                  </button>
                </div>
              )}
            </div>

            {/* Assign Subscription */}
            <div className="rounded-xl border border-line bg-surface p-5 space-y-4">
              <h3 className="text-sm font-bold text-ink">Assign Subscription Plan</h3>
              <form onSubmit={handleAssignSubscription} className="space-y-3">
                <div>
                  <label className="label">Select Plan</label>
                  <select
                    value={assignPlanId}
                    onChange={(e) => setAssignPlanId(e.target.value)}
                    className="input text-xs"
                  >
                    {plans.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.name} (Rs {p.pricePerMonth}/mo - {p.memberLimit || 'Unlimited'} members)
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="label">Duration (Months)</label>
                  <input
                    type="number"
                    min="1"
                    max="36"
                    value={assignMonths}
                    onChange={(e) => setAssignMonths(e.target.value)}
                    className="input text-xs"
                  />
                </div>

                <button
                  type="submit"
                  disabled={busyAction}
                  className="btn-primary text-xs"
                  style={{ width: 'auto', paddingInline: 20 }}
                >
                  {busyAction ? 'Assigning...' : 'Assign Subscription'}
                </button>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

