import { useEffect, useState, useCallback } from 'react'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'

const emptyPlanForm = {
  name: '',
  pricePerMonth: '',
  memberLimit: '',
  govIdVerification: false,
  status: 'ACTIVE',
}

export default function AdminPlans() {
  const [plans, setPlans] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Form mode: null (hidden), 'add', or an editing plan object
  const [formMode, setFormMode] = useState(null)
  const [formData, setFormData] = useState(emptyPlanForm)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const loadPlans = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/api/admin/plans')
      setPlans(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadPlans()
  }, [loadPlans])

  const handleStartAdd = () => {
    setFormData(emptyPlanForm)
    setFormMode('add')
    setFormError('')
    setSuccess('')
  }

  const handleStartEdit = (p) => {
    setFormData({
      name: p.name || '',
      pricePerMonth: p.pricePerMonth ?? '',
      memberLimit: p.memberLimit ?? '',
      govIdVerification: Boolean(p.govIdVerification),
      status: p.status || 'ACTIVE',
    })
    setFormMode(p)
    setFormError('')
    setSuccess('')
  }

  const handleCancelForm = () => {
    setFormMode(null)
    setFormData(emptyPlanForm)
    setFormError('')
  }

  const handleSavePlan = async (e) => {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    setSuccess('')

    const payload = {
      name: formData.name.trim(),
      pricePerMonth: Number(formData.pricePerMonth) || 0,
      memberLimit: formData.memberLimit !== '' && Number(formData.memberLimit) > 0
        ? Number(formData.memberLimit)
        : null,
      govIdVerification: Boolean(formData.govIdVerification),
      status: formData.status,
    }

    try {
      if (formMode === 'add') {
        await api.post('/api/admin/plans', payload)
        setSuccess('New subscription plan created successfully.')
      } else {
        await api.put(`/api/admin/plans/${formMode.id}`, payload)
        setSuccess(`Plan ${payload.name} updated successfully.`)
      }
      setFormMode(null)
      loadPlans()
    } catch (err) {
      setFormError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-ink">Subscription plans</h1>
          <p className="mt-1 text-sm text-muted">Bureau membership tiers offered on the platform</p>
        </div>

        {!formMode && (
          <button
            type="button"
            onClick={handleStartAdd}
            className="btn-primary"
            style={{ width: 'auto', paddingInline: 20 }}
          >
            + Add plan
          </button>
        )}
      </div>

      {error && (
        <p className="rounded-lg bg-red-50 p-3 text-sm text-red-600">{error}</p>
      )}

      {success && (
        <p className="rounded-lg bg-green-50 p-3 text-sm text-ok font-medium">{success}</p>
      )}

      {/* Plans Table (Design 14) */}
      <div className="card overflow-hidden">
        {loading ? (
          <p className="p-8 text-center text-sm text-muted">Loading plans...</p>
        ) : plans.length === 0 ? (
          <div className="p-12 text-center text-sm text-muted">
            No subscription plans found. Click &quot;+ Add plan&quot; to configure one.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-surface text-xs font-semibold text-muted border-b border-line">
                <tr>
                  <th className="px-6 py-3">PLAN</th>
                  <th className="px-6 py-3">PRICE</th>
                  <th className="px-6 py-3">MEMBER LIMIT</th>
                  <th className="px-6 py-3">GOV ID VERIFICATION</th>
                  <th className="px-6 py-3">STATUS</th>
                  <th className="px-6 py-3 text-right">ACTION</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {plans.map((p) => (
                  <tr key={p.id} className="hover:bg-surface/50">
                    <td className="px-6 py-4 font-bold text-ink">{p.name}</td>
                    <td className="px-6 py-4 font-serif text-ink">Rs {p.pricePerMonth}</td>
                    <td className="px-6 py-4 text-muted">
                      {p.memberLimit ? p.memberLimit : 'Unlimited'}
                    </td>
                    <td className="px-6 py-4">
                      {p.govIdVerification ? (
                        <span className="text-xs font-semibold text-ok">Yes</span>
                      ) : (
                        <span className="text-xs text-muted">No</span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                          p.status === 'ACTIVE'
                            ? 'bg-green-50 text-ok'
                            : 'bg-sand text-muted'
                        }`}
                      >
                        {p.status || 'Active'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        type="button"
                        onClick={() => handleStartEdit(p)}
                        className="btn-outline text-xs"
                        style={{ paddingBlock: 4, paddingInline: 12 }}
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Edit / Add Plan Card (Design 14) */}
      {formMode && (
        <div className="card p-6 sm:p-8 space-y-6">
          <div className="border-b border-line pb-4 flex items-center justify-between">
            <h2 className="text-xl font-bold text-ink">
              {formMode === 'add' ? 'Add new plan' : `Edit plan: ${formMode.name}`}
            </h2>
            <button
              type="button"
              onClick={handleCancelForm}
              className="text-xs font-semibold text-muted hover:text-ink"
            >
              Cancel ✕
            </button>
          </div>

          {formError && (
            <p className="rounded bg-red-50 p-2.5 text-xs text-red-600">{formError}</p>
          )}

          <form onSubmit={handleSavePlan} className="space-y-5">
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <label className="label">Plan name</label>
                <input
                  type="text"
                  required
                  maxLength={50}
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="e.g. Pro"
                  className="input"
                />
              </div>

              <div>
                <label className="label">Price per month (Rs)</label>
                <input
                  type="number"
                  min="0"
                  required
                  value={formData.pricePerMonth}
                  onChange={(e) => setFormData({ ...formData, pricePerMonth: e.target.value })}
                  placeholder="499"
                  className="input"
                />
              </div>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <label className="label">Member limit (leave empty or 0 for unlimited)</label>
                <input
                  type="number"
                  min="0"
                  value={formData.memberLimit}
                  onChange={(e) => setFormData({ ...formData, memberLimit: e.target.value })}
                  placeholder="e.g. 500"
                  className="input"
                />
              </div>

              <div>
                <label className="label">Status</label>
                <select
                  value={formData.status}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                  className="input"
                >
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                </select>
              </div>
            </div>

            {/* Gov ID verification switch */}
            <div className="rounded-xl border border-line bg-surface p-4 flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold text-ink">Government ID verification</p>
                <p className="text-xs text-muted">Allow hosts on this plan to verify IDs (Level 3).</p>
              </div>
              <input
                type="checkbox"
                checked={formData.govIdVerification}
                onChange={(e) => setFormData({ ...formData, govIdVerification: e.target.checked })}
                className="h-5 w-5 rounded text-brand focus:ring-brand"
              />
            </div>

            <div className="flex gap-3 pt-2">
              <button
                type="submit"
                disabled={saving}
                className="btn-primary"
                style={{ width: 'auto', paddingInline: 24 }}
              >
                {saving ? 'Saving...' : 'Save plan'}
              </button>
              <button
                type="button"
                onClick={handleCancelForm}
                className="btn-outline"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}

