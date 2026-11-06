import { useEffect, useState } from 'react'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'
import { timeAgo } from '../utils/time'

const FALLBACK_UPGRADE_PLANS = [
  { id: 1, name: 'Basic', pricePerMonth: 0, memberLimit: 50 },
  { id: 2, name: 'Pro', pricePerMonth: 499, memberLimit: 500 },
  { id: 3, name: 'Enterprise', pricePerMonth: 999, memberLimit: null },
]

export default function HostBilling() {
  const [subscription, setSubscription] = useState(null)
  const [loadingSub, setLoadingSub] = useState(true)
  const [subError, setSubError] = useState('')

  // Sharing toggles
  const [sharing, setSharing] = useState({
    shareMembers: false,
    searchOtherBureaus: false,
    showBureauName: false,
  })
  const [savingSharing, setSavingSharing] = useState(false)
  const [sharingSuccess, setSharingSuccess] = useState('')
  const [sharingError, setSharingError] = useState('')

  // Audit logs
  const [auditLogs, setAuditLogs] = useState([])
  const [loadingLogs, setLoadingLogs] = useState(true)

  // Upgrade modal
  const [showUpgradeModal, setShowUpgradeModal] = useState(false)
  const [selectedPlanId, setSelectedPlanId] = useState(2)
  const [requestingUpgrade, setRequestingUpgrade] = useState(false)
  const [upgradeNotice, setUpgradeNotice] = useState('')
  const [upgradeError, setUpgradeError] = useState('')

  useEffect(() => {
    // 1. Subscription details
    api
      .get('/api/host/subscription')
      .then((res) => setSubscription(res.data))
      .catch((err) => setSubError(getErrorMessage(err)))
      .finally(() => setLoadingSub(false))

    // 2. Audit logs
    api
      .get('/api/host/audit-logs?limit=50')
      .then((res) => setAuditLogs(Array.isArray(res.data) ? res.data : []))
      .catch(() => setAuditLogs([]))
      .finally(() => setLoadingLogs(false))
  }, [])

  const handleSaveSharing = async (e) => {
    e.preventDefault()
    setSavingSharing(true)
    setSharingSuccess('')
    setSharingError('')
    try {
      await api.put('/api/host/sharing', sharing)
      setSharingSuccess('Sharing preferences saved successfully.')
    } catch (err) {
      if (err.response?.status === 403 || err.response?.data?.code === 'PLAN_RESTRICTED') {
        setSharingError('Upgrade to Pro to use cross-bureau sharing.')
      } else {
        setSharingError(getErrorMessage(err))
      }
    } finally {
      setSavingSharing(false)
    }
  }

  const handleRequestUpgrade = async () => {
    setRequestingUpgrade(true)
    setUpgradeNotice('')
    setUpgradeError('')
    try {
      await api.post('/api/host/subscription/upgrade', { planId: Number(selectedPlanId) })
      setUpgradeNotice('Upgrade request submitted successfully! Awaiting Super Admin approval.')
      setTimeout(() => setShowUpgradeModal(false), 2000)
    } catch (err) {
      setUpgradeError(getErrorMessage(err))
    } finally {
      setRequestingUpgrade(false)
    }
  }

  const renewalDateStr = subscription?.endDate
    ? new Date(subscription.endDate).toLocaleDateString('en-GB', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
      })
    : 'Ongoing'

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-ink">Billing and sharing</h1>
        <p className="mt-1 text-sm text-muted">Manage your bureau subscription and network settings</p>
      </div>

      <div className="grid gap-8 lg:grid-cols-2 items-start">
        {/* Left Column: Subscription & History */}
        <div className="space-y-6">
          {/* Current Plan Card (Design 12) */}
          <div className="card p-6 sm:p-8 space-y-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted">Current plan</span>
              <span
                className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                  subscription?.status === 'EXPIRED'
                    ? 'bg-red-50 text-red-600'
                    : 'bg-green-50 text-ok'
                }`}
              >
                {subscription?.status || 'Active'}
              </span>
            </div>

            {loadingSub ? (
              <p className="text-xs text-muted">Loading subscription...</p>
            ) : subError ? (
              <p className="text-xs text-red-600">{subError}</p>
            ) : (
              <div>
                <h2 className="text-2xl font-bold text-ink">{subscription?.planName || 'Basic Plan'}</h2>
                <p className="mt-1 text-sm text-muted">
                  Rs {subscription?.pricePerMonth ?? 0} / month
                </p>
                <p className="mt-3 text-xs text-muted">
                  Next billing: <span className="font-semibold text-ink">{renewalDateStr}</span>
                </p>
              </div>
            )}

            <div className="pt-2">
              <button
                type="button"
                onClick={() => setShowUpgradeModal(true)}
                className="btn-primary"
                style={{ width: 'auto', paddingInline: 24 }}
              >
                Upgrade plan
              </button>
            </div>
          </div>

          {/* Billing History (Design 12) */}
          <div className="card overflow-hidden">
            <div className="border-b border-line px-6 py-4">
              <h2 className="text-base font-bold text-ink">Billing history</h2>
            </div>
            <div className="p-8 text-center text-xs text-muted">
              No previous billing invoices yet. Future renewals will be logged here.
            </div>
          </div>
        </div>

        {/* Right Column: Sharing & Audit Log */}
        <div className="space-y-6">
          {/* Cross-bureau Sharing Card (Design 12) */}
          <div className="card p-6 sm:p-8 space-y-5">
            <div>
              <h2 className="text-lg font-bold text-ink">Cross-bureau sharing</h2>
              <p className="mt-1 text-xs text-muted">Opt in to share members with other bureaus.</p>
            </div>

            {sharingError && (
              <p className="rounded bg-red-50 p-2.5 text-xs text-red-600 font-medium">{sharingError}</p>
            )}
            {sharingSuccess && (
              <p className="rounded bg-green-50 p-2.5 text-xs text-ok font-medium">{sharingSuccess}</p>
            )}

            <form onSubmit={handleSaveSharing} className="space-y-4">
              <label className="flex items-center justify-between gap-4 cursor-pointer rounded-lg border border-line p-3 hover:bg-surface">
                <div>
                  <span className="text-sm font-medium text-ink">Share my members with other bureaus</span>
                  <p className="text-[11px] text-muted">Allow network bureaus to discover eligible candidates</p>
                </div>
                <input
                  type="checkbox"
                  checked={sharing.shareMembers}
                  onChange={(e) => setSharing({ ...sharing, shareMembers: e.target.checked })}
                  className="h-4 w-4 rounded text-brand"
                />
              </label>

              <label className="flex items-center justify-between gap-4 cursor-pointer rounded-lg border border-line p-3 hover:bg-surface">
                <div>
                  <span className="text-sm font-medium text-ink">Search members from other bureaus</span>
                  <p className="text-[11px] text-muted">Expand match results using partner candidate pools</p>
                </div>
                <input
                  type="checkbox"
                  checked={sharing.searchOtherBureaus}
                  onChange={(e) => setSharing({ ...sharing, searchOtherBureaus: e.target.checked })}
                  className="h-4 w-4 rounded text-brand"
                />
              </label>

              <label className="flex items-center justify-between gap-4 cursor-pointer rounded-lg border border-line p-3 hover:bg-surface">
                <div>
                  <span className="text-sm font-medium text-ink">Show my bureau name on shared profiles</span>
                  <p className="text-[11px] text-muted">Displays your branding on candidate cards</p>
                </div>
                <input
                  type="checkbox"
                  checked={sharing.showBureauName}
                  onChange={(e) => setSharing({ ...sharing, showBureauName: e.target.checked })}
                  className="h-4 w-4 rounded text-brand"
                />
              </label>

              <div className="pt-2">
                <button
                  type="submit"
                  disabled={savingSharing}
                  className="btn-primary"
                  style={{ width: 'auto', paddingInline: 24 }}
                >
                  {savingSharing ? 'Saving...' : 'Save settings'}
                </button>
              </div>
            </form>
          </div>

          {/* Audit Logs Preview */}
          <div className="card overflow-hidden">
            <div className="border-b border-line px-6 py-4">
              <h2 className="text-base font-bold text-ink">Recent activity & audit logs</h2>
            </div>
            {loadingLogs ? (
              <p className="p-6 text-center text-xs text-muted">Loading audit log...</p>
            ) : auditLogs.length === 0 ? (
              <p className="p-6 text-center text-xs text-muted">No audit activity recorded yet.</p>
            ) : (
              <div className="max-h-64 overflow-y-auto divide-y divide-line">
                {auditLogs.map((log) => (
                  <div key={log.id} className="px-6 py-3 text-xs">
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-ink">{log.action}</span>
                      <span className="text-muted">{timeAgo(log.createdAt)}</span>
                    </div>
                    {log.details && <p className="text-muted mt-0.5">{log.details}</p>}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Upgrade Request Modal */}
      {showUpgradeModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="card w-full max-w-md p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-line pb-3">
              <h2 className="text-lg font-bold text-ink">Request Plan Upgrade</h2>
              <button
                type="button"
                onClick={() => setShowUpgradeModal(false)}
                className="text-muted hover:text-ink text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {upgradeNotice && (
              <p className="rounded bg-green-50 p-2.5 text-xs text-ok font-medium">{upgradeNotice}</p>
            )}
            {upgradeError && (
              <p className="rounded bg-red-50 p-2.5 text-xs text-red-600">{upgradeError}</p>
            )}

            <div className="space-y-3">
              <p className="text-xs text-muted">
                Select your target plan. The request will be reviewed and assigned by the platform administrator.
              </p>

              {FALLBACK_UPGRADE_PLANS.map((p) => (
                <label
                  key={p.id}
                  className={`flex cursor-pointer items-center justify-between rounded-xl border p-3.5 transition ${
                    selectedPlanId === p.id
                      ? 'border-brand bg-brand-soft/30'
                      : 'border-line hover:bg-surface'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <input
                      type="radio"
                      name="upgradePlan"
                      value={p.id}
                      checked={selectedPlanId === p.id}
                      onChange={() => setSelectedPlanId(p.id)}
                      className="text-brand"
                    />
                    <div>
                      <p className="font-semibold text-ink">{p.name}</p>
                      <p className="text-[11px] text-muted">
                        {p.memberLimit ? `Up to ${p.memberLimit} members` : 'Unlimited members'}
                      </p>
                    </div>
                  </div>
                  <span className="font-serif font-bold text-ink">Rs {p.pricePerMonth}/mo</span>
                </label>
              ))}
            </div>

            <div className="flex justify-end gap-3 pt-3">
              <button
                type="button"
                onClick={() => setShowUpgradeModal(false)}
                className="btn-outline text-xs"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleRequestUpgrade}
                disabled={requestingUpgrade}
                className="btn-primary"
                style={{ width: 'auto', paddingInline: 20 }}
              >
                {requestingUpgrade ? 'Submitting...' : 'Send Upgrade Request'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

