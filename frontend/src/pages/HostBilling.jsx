import { useCallback, useEffect, useRef, useState } from 'react'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'
import { getErrorMessage } from '../utils/errors'
import { timeAgo } from '../utils/time'
import { loadRazorpay } from '../utils/razorpay'

const MONTH_OPTIONS = [1, 3, 6, 12]

const STATUS_STYLE = {
  PAID: ['Paid', 'bg-green-50 text-ok'],
  FAILED: ['Failed', 'bg-red-50 text-red-600'],
  CREATED: ['Not completed', 'bg-amber-50 text-warn'],
}

// axios errors come from the server; anything else already has a readable message
const messageOf = (err) => (err.response ? getErrorMessage(err) : err.message || 'Something went wrong')

const formatDate = (value) =>
  value
    ? new Date(value).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
    : '-'

export default function HostBilling() {
  const { email } = useAuth()

  const [subscription, setSubscription] = useState(null)
  const [loadingSub, setLoadingSub] = useState(true)
  const [subError, setSubError] = useState('')

  // Billing history
  const [payments, setPayments] = useState([])
  const [loadingPayments, setLoadingPayments] = useState(true)

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

  // Pay modal
  const [showPayModal, setShowPayModal] = useState(false)
  const [plans, setPlans] = useState([])
  const [plansError, setPlansError] = useState('')
  const [selectedPlanId, setSelectedPlanId] = useState(null)
  const [months, setMonths] = useState(1)
  const [paying, setPaying] = useState(false)
  const [payError, setPayError] = useState('')
  const [paySuccess, setPaySuccess] = useState('')
  const paidRef = useRef(false)

  const loadSubscription = useCallback(() => {
    return api
      .get('/api/host/subscription')
      .then((res) => {
        setSubscription(res.data)
        setSubError('')
      })
      .catch((err) => setSubError(getErrorMessage(err)))
      .finally(() => setLoadingSub(false))
  }, [])

  const loadPayments = useCallback(() => {
    return api
      .get('/api/host/payments')
      .then((res) => setPayments(Array.isArray(res.data) ? res.data : []))
      .catch(() => setPayments([]))
      .finally(() => setLoadingPayments(false))
  }, [])

  useEffect(() => {
    loadSubscription()
    loadPayments()

    api
      .get('/api/host/audit-logs?limit=50')
      .then((res) => setAuditLogs(Array.isArray(res.data) ? res.data : []))
      .catch(() => setAuditLogs([]))
      .finally(() => setLoadingLogs(false))

    api
      .get('/api/plans')
      .then((res) => setPlans(Array.isArray(res.data) ? res.data : []))
      .catch((err) => setPlansError(getErrorMessage(err)))
  }, [loadSubscription, loadPayments])

  // only plans with a price can be paid for
  const paidPlans = plans.filter((p) => Number(p.pricePerMonth) > 0)
  const chosenId = selectedPlanId ?? paidPlans[0]?.id ?? null
  const chosenPlan = paidPlans.find((p) => p.id === chosenId)
  const total = chosenPlan ? Number(chosenPlan.pricePerMonth) * months : 0

  const openPayModal = () => {
    setPayError('')
    setPaySuccess('')
    paidRef.current = false
    setShowPayModal(true)
  }

  const closePayModal = () => {
    if (paying) return
    setShowPayModal(false)
  }

  const handlePay = async () => {
    if (!chosenPlan) return
    setPaying(true)
    setPayError('')
    setPaySuccess('')
    paidRef.current = false

    let order
    try {
      order = (await api.post('/api/host/payments/create-order', { planId: chosenPlan.id, months })).data
      const Razorpay = await loadRazorpay()

      const checkout = new Razorpay({
        key: order.keyId,
        amount: order.amount,
        currency: order.currency,
        name: 'TrustMatch',
        description: `${order.planName} plan, ${order.months} month(s)`,
        order_id: order.orderId,
        prefill: { email },
        theme: { color: '#6b1d2e' },
        handler: async (response) => {
          // Razorpay says it is paid; the backend checks the signature before activating the plan
          paidRef.current = true
          try {
            await api.post('/api/host/payments/verify', {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            })
            setPaySuccess(`Payment successful. Your ${order.planName} plan is now active.`)
            await Promise.all([loadSubscription(), loadPayments()])
          } catch (err) {
            setPayError(messageOf(err))
            await loadPayments()
          } finally {
            setPaying(false)
          }
        },
        modal: {
          ondismiss: async () => {
            // the host closed the popup without paying
            if (paidRef.current) return
            try {
              await api.post('/api/host/payments/fail', { razorpayOrderId: order.orderId })
            } catch {
              // nothing to do if marking fails
            }
            setPayError('Payment was cancelled. You have not been charged.')
            setPaying(false)
            loadPayments()
          },
        },
      })

      checkout.on('payment.failed', (resp) => {
        setPayError(resp?.error?.description || 'Payment failed. Please try again.')
      })

      checkout.open()
    } catch (err) {
      setPayError(messageOf(err))
      setPaying(false)
    }
  }

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

  const renewalDateStr = subscription?.endDate ? formatDate(subscription.endDate) : 'Ongoing'

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-ink">Billing and sharing</h1>
        <p className="mt-1 text-sm text-muted">Manage your bureau subscription and network settings</p>
      </div>

      <div className="grid gap-8 lg:grid-cols-2 items-start">
        {/* Left Column: Subscription & History */}
        <div className="space-y-6">
          {/* Current Plan Card */}
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
                onClick={openPayModal}
                className="btn-primary"
                style={{ width: 'auto', paddingInline: 24 }}
              >
                Upgrade or renew plan
              </button>
            </div>
          </div>

          {/* Billing History */}
          <div className="card overflow-hidden">
            <div className="border-b border-line px-6 py-4">
              <h2 className="text-base font-bold text-ink">Billing history</h2>
            </div>
            {loadingPayments ? (
              <p className="p-6 text-center text-xs text-muted">Loading...</p>
            ) : payments.length === 0 ? (
              <div className="p-8 text-center text-xs text-muted">
                No payments yet. Your payments will be listed here.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-surface text-muted">
                    <tr>
                      <th className="px-4 py-2 font-semibold">Date</th>
                      <th className="px-4 py-2 font-semibold">Plan</th>
                      <th className="px-4 py-2 font-semibold">Amount</th>
                      <th className="px-4 py-2 font-semibold">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line">
                    {payments.map((p) => {
                      const [label, cls] = STATUS_STYLE[p.status] || [p.status, 'bg-surface text-muted']
                      return (
                        <tr key={p.id}>
                          <td className="px-4 py-3 text-muted">{formatDate(p.paidAt || p.createdAt)}</td>
                          <td className="px-4 py-3 font-semibold text-ink">
                            {p.planName}
                            <span className="block font-normal text-muted">{p.months} month(s)</span>
                          </td>
                          <td className="px-4 py-3 text-ink">Rs {p.amount}</td>
                          <td className="px-4 py-3">
                            <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${cls}`}>
                              {label}
                            </span>
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Sharing & Audit Log */}
        <div className="space-y-6">
          {/* Cross-bureau Sharing Card */}
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

      {/* Pay Modal */}
      {showPayModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="card w-full max-w-md space-y-4 p-6">
            <div className="flex items-center justify-between border-b border-line pb-3">
              <h2 className="text-lg font-bold text-ink">Upgrade or renew plan</h2>
              <button
                type="button"
                onClick={closePayModal}
                disabled={paying}
                aria-label="Close"
                className="text-sm font-bold text-muted hover:text-ink"
              >
                ✕
              </button>
            </div>

            {paySuccess && (
              <p className="rounded bg-green-50 p-2.5 text-xs font-medium text-ok">{paySuccess}</p>
            )}
            {payError && (
              <p className="rounded bg-red-50 p-2.5 text-xs text-red-600">{payError}</p>
            )}

            {paySuccess ? (
              <div className="flex justify-end pt-2">
                <button
                  type="button"
                  onClick={() => setShowPayModal(false)}
                  className="btn-primary"
                  style={{ width: 'auto', paddingInline: 20 }}
                >
                  Done
                </button>
              </div>
            ) : (
              <>
                <div className="space-y-3">
                  {plansError && <p className="text-xs text-red-600">{plansError}</p>}
                  {!plansError && paidPlans.length === 0 && (
                    <p className="text-xs text-muted">No paid plans are available right now.</p>
                  )}

                  {paidPlans.map((p) => (
                    <label
                      key={p.id}
                      className={`flex cursor-pointer items-center justify-between rounded-xl border p-3.5 transition ${
                        chosenId === p.id ? 'border-brand bg-brand-soft/30' : 'border-line hover:bg-surface'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <input
                          type="radio"
                          name="payPlan"
                          value={p.id}
                          checked={chosenId === p.id}
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

                  <div>
                    <label className="label">Duration</label>
                    <select
                      className="input"
                      value={months}
                      onChange={(e) => setMonths(Number(e.target.value))}
                    >
                      {MONTH_OPTIONS.map((m) => (
                        <option key={m} value={m}>
                          {m} month{m > 1 ? 's' : ''}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <p className="text-[11px] text-muted">
                  Test mode: no real money is charged. Paying replaces your current plan and starts a new period from today.
                </p>

                <div className="flex justify-end gap-3 pt-2">
                  <button type="button" onClick={closePayModal} disabled={paying} className="btn-outline text-xs">
                    Cancel
                  </button>
                  <button
                    type="button"
                    onClick={handlePay}
                    disabled={paying || !chosenPlan}
                    className="btn-primary"
                    style={{ width: 'auto', paddingInline: 20 }}
                  >
                    {paying ? 'Processing...' : `Pay Rs ${total}`}
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  )
}