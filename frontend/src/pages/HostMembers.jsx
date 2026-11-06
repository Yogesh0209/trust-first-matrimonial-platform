import { useEffect, useState, useCallback } from 'react'
import { useSearchParams } from 'react-router-dom'
import api from '../api/axios'
import TrustBadge from '../components/TrustBadge'
import { getErrorMessage } from '../utils/errors'
import { timeAgo } from '../utils/time'
import SecureImage from '../components/SecureImage'

function StatusPill({ status }) {
  const styles = {
    ACTIVE: 'bg-green-50 text-ok',
    PAUSED: 'bg-amber-50 text-warn',
    SUSPENDED: 'bg-red-50 text-red-600',
    PENDING: 'bg-sand text-muted',
    HIDDEN: 'bg-sand text-muted',
  }
  const label = status ? status.charAt(0) + status.slice(1).toLowerCase() : 'Unknown'
  return (
    <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${styles[status] || 'bg-sand text-muted'}`}>
      {label}
    </span>
  )
}

export default function HostMembers() {
  const [searchParams, setSearchParams] = useSearchParams()
  const activeTab = searchParams.get('tab') === 'verifications' ? 'verifications' : 'members'

  // Tab 1 state: All members
  const [members, setMembers] = useState([])
  const [loadingMembers, setLoadingMembers] = useState(true)
  const [membersError, setMembersError] = useState('')

  // Add Member Modal state
  const [showAddModal, setShowAddModal] = useState(false)
  const [addForm, setAddForm] = useState({
    fullName: '',
    email: '',
    phone: '',
    password: '',
    gender: 'MALE',
    dateOfBirth: '',
  })
  const [addingMember, setAddingMember] = useState(false)
  const [addError, setAddError] = useState('')

  // Status Change Dialog state
  const [statusDialog, setStatusDialog] = useState({
    open: false,
    member: null,
    targetStatus: '',
    reason: '',
  })
  const [changingStatus, setChangingStatus] = useState(false)
  const [statusError, setStatusError] = useState('')

  // Tab 2 state: Pending verifications
  const [verifications, setVerifications] = useState([])
  const [loadingVerifications, setLoadingVerifications] = useState(true)
  const [verificationsError, setVerificationsError] = useState('')
  const [selectedVer, setSelectedVer] = useState(null)
  const [rejectReason, setRejectReason] = useState('')
  const [processingDecision, setProcessingDecision] = useState(false)
  const [decisionError, setDecisionError] = useState('')
  const [failedImgs, setFailedImgs] = useState({})

  // Load members
  const loadMembers = useCallback(async () => {
    setLoadingMembers(true)
    setMembersError('')
    try {
      const res = await api.get('/api/host/members')
      setMembers(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      setMembersError(getErrorMessage(err))
    } finally {
      setLoadingMembers(false)
    }
  }, [])

  // Load pending verifications
  const loadVerifications = useCallback(async () => {
    setLoadingVerifications(true)
    setVerificationsError('')
    try {
      const res = await api.get('/api/host/verifications')
      const list = Array.isArray(res.data) ? res.data : []
      setVerifications(list)
      if (list.length > 0 && !selectedVer) {
        setSelectedVer(list[0])
      }
    } catch (err) {
      setVerificationsError(getErrorMessage(err))
    } finally {
      setLoadingVerifications(false)
    }
  }, [selectedVer])

  useEffect(() => {
    loadMembers()
    loadVerifications()
  }, [loadMembers, loadVerifications])

  // Handle Add Member submit
  const handleAddMember = async (e) => {
    e.preventDefault()
    setAddingMember(true)
    setAddError('')
    try {
      await api.post('/api/host/members', addForm)
      setShowAddModal(false)
      setAddForm({
        fullName: '',
        email: '',
        phone: '',
        password: '',
        gender: 'MALE',
        dateOfBirth: '',
      })
      await loadMembers()
    } catch (err) {
      setAddError(getErrorMessage(err))
    } finally {
      setAddingMember(false)
    }
  }

  // Handle Member Status Change (ACTIVE / PAUSED / SUSPENDED)
  const handleStatusSubmit = async (e) => {
    e.preventDefault()
    if (!statusDialog.member) return
    if (!statusDialog.reason.trim()) {
      setStatusError('A reason is required to change member status.')
      return
    }

    setChangingStatus(true)
    setStatusError('')
    try {
      await api.put(`/api/host/members/${statusDialog.member.userId}/status`, {
        status: statusDialog.targetStatus,
        reason: statusDialog.reason.trim(),
      })
      setStatusDialog({ open: false, member: null, targetStatus: '', reason: '' })
      await loadMembers()
    } catch (err) {
      setStatusError(getErrorMessage(err))
    } finally {
      setChangingStatus(false)
    }
  }

  // Handle Verification Decision (APPROVE / REJECT)
  const handleDecision = async (decision) => {
    if (!selectedVer) return
    if (decision === 'REJECTED' && !rejectReason.trim()) {
      setDecisionError('Please provide a reason when rejecting a verification.')
      return
    }

    setProcessingDecision(true)
    setDecisionError('')
    try {
      await api.put(`/api/host/verifications/${selectedVer.id}`, {
        decision,
        reason: rejectReason.trim() || undefined,
      })
      setRejectReason('')
      // Remove reviewed item from list
      const updated = verifications.filter((v) => v.id !== selectedVer.id)
      setVerifications(updated)
      setSelectedVer(updated[0] || null)
    } catch (err) {
      setDecisionError(getErrorMessage(err))
    } finally {
      setProcessingDecision(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-ink">Members and verification</h1>
          <p className="mt-1 text-sm text-muted">Manage candidate profiles and identity reviews</p>
        </div>

        {activeTab === 'members' && (
          <button
            type="button"
            onClick={() => setShowAddModal(true)}
            className="btn-primary"
            style={{ width: 'auto', paddingInline: 20 }}
          >
            + Add member
          </button>
        )}
      </div>

      {/* Tabs */}
      <div className="flex gap-8 border-b border-line">
        <button
          type="button"
          onClick={() => setSearchParams({ tab: 'members' })}
          className={`border-b-2 pb-2 text-sm font-semibold transition ${activeTab === 'members'
              ? 'border-brand text-brand'
              : 'border-transparent text-muted hover:text-ink'
            }`}
        >
          All members ({members.length})
        </button>
        <button
          type="button"
          onClick={() => setSearchParams({ tab: 'verifications' })}
          className={`flex items-center gap-2 border-b-2 pb-2 text-sm font-semibold transition ${activeTab === 'verifications'
              ? 'border-brand text-brand'
              : 'border-transparent text-muted hover:text-ink'
            }`}
        >
          Pending verification
          {verifications.length > 0 && (
            <span className="rounded-full bg-brand-soft px-2 py-0.5 text-xs text-brand font-bold">
              {verifications.length}
            </span>
          )}
        </button>
      </div>

      {/* Tab 1: All Members */}
      {activeTab === 'members' && (
        <div className="card overflow-hidden">
          {membersError && (
            <p className="m-4 rounded bg-red-50 p-3 text-xs text-red-600">{membersError}</p>
          )}

          {loadingMembers ? (
            <p className="p-8 text-center text-sm text-muted">Loading members list...</p>
          ) : members.length === 0 ? (
            <div className="p-12 text-center text-sm text-muted">
              No members registered yet. Click &quot;+ Add member&quot; to register your first candidate.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-surface text-xs font-semibold text-muted border-b border-line">
                  <tr>
                    <th className="px-6 py-3">NAME & CONTACT</th>
                    <th className="px-6 py-3">CITY</th>
                    <th className="px-6 py-3">STATUS</th>
                    <th className="px-6 py-3">TRUST LEVEL</th>
                    <th className="px-6 py-3">LAST ACTIVE</th>
                    <th className="px-6 py-3 text-right">ACTIONS</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-line">
                  {members.map((m) => (
                    <tr key={m.userId} className="hover:bg-surface/50">
                      <td className="px-6 py-4">
                        <p className="font-semibold text-ink">{m.fullName}</p>
                        <p className="text-xs text-muted">{m.email} {m.phone ? `• ${m.phone}` : ''}</p>
                      </td>
                      <td className="px-6 py-4 text-muted">{m.city || '—'}</td>
                      <td className="px-6 py-4">
                        <StatusPill status={m.status} />
                      </td>
                      <td className="px-6 py-4">
                        <TrustBadge level={m.trustLevel || 1} />
                      </td>
                      <td className="px-6 py-4 text-xs text-muted">
                        {timeAgo(m.lastActiveAt || m.createdAt)}
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="flex justify-end gap-2">
                          {m.status === 'ACTIVE' && (
                            <>
                              <button
                                type="button"
                                onClick={() =>
                                  setStatusDialog({
                                    open: true,
                                    member: m,
                                    targetStatus: 'PAUSED',
                                    reason: '',
                                  })
                                }
                                className="btn-outline text-xs"
                                style={{ paddingBlock: 4, paddingInline: 10 }}
                              >
                                Pause
                              </button>
                              <button
                                type="button"
                                onClick={() =>
                                  setStatusDialog({
                                    open: true,
                                    member: m,
                                    targetStatus: 'SUSPENDED',
                                    reason: '',
                                  })
                                }
                                className="rounded border border-red-200 bg-red-50 px-2.5 py-1 text-xs font-semibold text-red-600 hover:bg-red-100"
                              >
                                Suspend
                              </button>
                            </>
                          )}

                          {m.status === 'PAUSED' && (
                            <>
                              <button
                                type="button"
                                onClick={() =>
                                  setStatusDialog({
                                    open: true,
                                    member: m,
                                    targetStatus: 'ACTIVE',
                                    reason: '',
                                  })
                                }
                                className="btn-primary text-xs"
                                style={{ width: 'auto', paddingBlock: 4, paddingInline: 12 }}
                              >
                                Activate
                              </button>
                              <button
                                type="button"
                                onClick={() =>
                                  setStatusDialog({
                                    open: true,
                                    member: m,
                                    targetStatus: 'SUSPENDED',
                                    reason: '',
                                  })
                                }
                                className="rounded border border-red-200 bg-red-50 px-2.5 py-1 text-xs font-semibold text-red-600 hover:bg-red-100"
                              >
                                Suspend
                              </button>
                            </>
                          )}

                          {m.status === 'SUSPENDED' && (
                            <button
                              type="button"
                              onClick={() =>
                                setStatusDialog({
                                  open: true,
                                  member: m,
                                  targetStatus: 'ACTIVE',
                                  reason: '',
                                })
                              }
                              className="btn-primary text-xs"
                              style={{ width: 'auto', paddingBlock: 4, paddingInline: 12 }}
                            >
                              Re-activate
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Tab 2: Pending Verifications (Design 10 Split Screen) */}
      {activeTab === 'verifications' && (
        <div className="grid gap-6 lg:grid-cols-12 items-start">
          {/* Left List */}
          <div className="card overflow-hidden lg:col-span-7">
            <div className="border-b border-line px-5 py-3.5 bg-surface">
              <h2 className="text-sm font-bold text-ink">Verification Queue</h2>
            </div>

            {verificationsError && (
              <p className="m-3 rounded bg-red-50 p-2 text-xs text-red-600">{verificationsError}</p>
            )}

            {loadingVerifications ? (
              <p className="p-8 text-center text-xs text-muted">Loading queue...</p>
            ) : verifications.length === 0 ? (
              <div className="p-12 text-center text-xs text-muted">
                No pending verification requests at this time. All caught up!
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="bg-surface text-xs font-semibold text-muted border-b border-line">
                    <tr>
                      <th className="px-5 py-3">NAME</th>
                      <th className="px-5 py-3">REQUEST</th>
                      <th className="px-5 py-3">SUBMITTED</th>
                      <th className="px-5 py-3 text-right">ACTION</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line">
                    {verifications.map((v) => {
                      const isSelected = selectedVer?.id === v.id
                      return (
                        <tr
                          key={v.id}
                          onClick={() => setSelectedVer(v)}
                          className={`cursor-pointer transition ${isSelected ? 'bg-brand-soft/40 font-medium' : 'hover:bg-surface/50'
                            }`}
                        >
                          <td className="px-5 py-3.5 text-ink">
                            {v.fullName || `Profile #${v.profileId}`}
                          </td>
                          <td className="px-5 py-3.5 text-xs text-muted">
                            {v.type === 'GOV_ID' ? 'Gov ID' : 'Selfie'}
                          </td>
                          <td className="px-5 py-3.5 text-xs text-muted">
                            {timeAgo(v.submittedAt)}
                          </td>
                          <td className="px-5 py-3.5 text-right">
                            <button
                              type="button"
                              className={isSelected ? 'btn-primary text-xs' : 'btn-outline text-xs'}
                              style={{ width: 'auto', paddingBlock: 3, paddingInline: 10 }}
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

          {/* Right Review Panel */}
          <div className="card p-6 lg:col-span-5">
            {selectedVer ? (
              <div className="space-y-6">
                <div>
                  <h3 className="text-base font-bold text-ink">
                    Review: {selectedVer.fullName || `Profile #${selectedVer.profileId}`}
                  </h3>
                  <p className="text-xs text-muted mt-0.5">
                    {selectedVer.type === 'GOV_ID' ? 'Government ID request' : 'Selfie request'} • Submitted {timeAgo(selectedVer.submittedAt)}
                  </p>
                </div>

                {decisionError && (
                  <p className="rounded bg-red-50 p-2 text-xs text-red-600">{decisionError}</p>
                )}

                {/* Submitted Media */}
                <div className="grid grid-cols-2 gap-3">
                  <div className="text-center">
                    <div className="aspect-square w-full rounded-xl border border-line bg-surface overflow-hidden flex items-center justify-center">
                      <SecureImage
                        key={`${selectedVer.id}-front`}
                        src={selectedVer.fileUrl}
                        alt="Submitted verification"
                        className="h-full w-full object-cover"
                        fallback={<span className="text-xs text-muted">No photo</span>}
                      />
                    </div>
                    <span className="mt-1.5 block text-xs font-medium text-muted">
                      {selectedVer.type === 'GOV_ID' ? 'ID Front' : 'Live selfie'}
                    </span>
                  </div>

                  <div className="text-center">
                    <div className="aspect-square w-full rounded-xl border border-line bg-surface overflow-hidden flex items-center justify-center">
                      <SecureImage
                        key={`${selectedVer.id}-back`}
                        src={selectedVer.fileUrlBack}
                        alt="ID Back"
                        className="h-full w-full object-cover"
                        fallback={<span className="text-xs text-muted">Profile photo / Back</span>}
                      />
                    </div>
                    <span className="mt-1.5 block text-xs font-medium text-muted">
                      {selectedVer.type === 'GOV_ID' ? 'ID Back' : 'Profile photo'}
                    </span>
                  </div>
                </div>

                {/* Verification Checklist */}
                <div className="rounded-xl border border-line bg-surface p-4 text-xs space-y-2">
                  <p className="font-semibold text-ink">Check before approving:</p>
                  <label className="flex items-center gap-2 text-muted cursor-pointer">
                    <input type="checkbox" defaultChecked className="rounded text-brand" />
                    <span>Same person in all submitted photos</span>
                  </label>
                  <label className="flex items-center gap-2 text-muted cursor-pointer">
                    <input type="checkbox" defaultChecked className="rounded text-brand" />
                    <span>Photo is clear, legible, and not a screenshot</span>
                  </label>
                  <label className="flex items-center gap-2 text-muted cursor-pointer">
                    <input type="checkbox" defaultChecked className="rounded text-brand" />
                    <span>No heavy beauty filters or alterations</span>
                  </label>
                </div>

                {/* Rejection reason box */}
                <div>
                  <label className="label">Reason (if rejecting)</label>
                  <input
                    type="text"
                    value={rejectReason}
                    onChange={(e) => setRejectReason(e.target.value)}
                    placeholder="Write a clear reason for rejection..."
                    maxLength={255}
                    className="input text-xs"
                  />
                </div>

                {/* Action buttons */}
                <div className="flex gap-3">
                  <button
                    type="button"
                    onClick={() => handleDecision('APPROVED')}
                    disabled={processingDecision}
                    className="btn-primary flex-1"
                  >
                    {processingDecision ? 'Approving...' : 'Approve'}
                  </button>
                  <button
                    type="button"
                    onClick={() => handleDecision('REJECTED')}
                    disabled={processingDecision}
                    className="btn-outline flex-1 text-red-600 hover:border-red-300 hover:bg-red-50"
                  >
                    {processingDecision ? 'Rejecting...' : 'Reject'}
                  </button>
                </div>
              </div>
            ) : (
              <div className="p-8 text-center text-xs text-muted">
                Select a verification request from the left to view details and approve.
              </div>
            )}
          </div>
        </div>
      )}

      {/* Add Member Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="card w-full max-w-md p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-line pb-3">
              <h2 className="text-lg font-bold text-ink">Add New Bureau Member</h2>
              <button
                type="button"
                onClick={() => setShowAddModal(false)}
                className="text-muted hover:text-ink text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {addError && (
              <p className="rounded bg-red-50 p-2 text-xs text-red-600">{addError}</p>
            )}

            <form onSubmit={handleAddMember} className="space-y-3">
              <div>
                <label className="label">Full Name</label>
                <input
                  type="text"
                  required
                  value={addForm.fullName}
                  onChange={(e) => setAddForm({ ...addForm, fullName: e.target.value })}
                  placeholder="e.g. Ramesh Patel"
                  className="input"
                />
              </div>

              <div>
                <label className="label">Email Address</label>
                <input
                  type="email"
                  required
                  value={addForm.email}
                  onChange={(e) => setAddForm({ ...addForm, email: e.target.value })}
                  placeholder="ramesh@example.com"
                  className="input"
                />
              </div>

              <div>
                <label className="label">Phone Number</label>
                <input
                  type="tel"
                  required
                  value={addForm.phone}
                  onChange={(e) => setAddForm({ ...addForm, phone: e.target.value })}
                  placeholder="+91 9876543210"
                  className="input"
                />
              </div>

              <div>
                <label className="label">Initial Password</label>
                <input
                  type="password"
                  required
                  value={addForm.password}
                  onChange={(e) => setAddForm({ ...addForm, password: e.target.value })}
                  placeholder="Temporary password"
                  className="input"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="label">Gender</label>
                  <select
                    value={addForm.gender}
                    onChange={(e) => setAddForm({ ...addForm, gender: e.target.value })}
                    className="input"
                  >
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
                <div>
                  <label className="label">Date of Birth</label>
                  <input
                    type="date"
                    required
                    value={addForm.dateOfBirth}
                    onChange={(e) => setAddForm({ ...addForm, dateOfBirth: e.target.value })}
                    className="input"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="btn-outline"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={addingMember}
                  className="btn-primary"
                  style={{ width: 'auto', paddingInline: 20 }}
                >
                  {addingMember ? 'Adding...' : 'Create Member'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Member Status Change / Suspend Confirmation Modal (Design 20) */}
      {statusDialog.open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="card w-full max-w-md p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-line pb-3">
              <h2 className="text-lg font-bold text-ink">
                {statusDialog.targetStatus === 'SUSPENDED'
                  ? 'Remove this profile?'
                  : `Change Status to ${statusDialog.targetStatus}`}
              </h2>
              <button
                type="button"
                onClick={() =>
                  setStatusDialog({ open: false, member: null, targetStatus: '', reason: '' })
                }
                className="text-muted hover:text-ink text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {statusDialog.targetStatus === 'SUSPENDED' && (
              <div className="flex items-start gap-3 rounded-xl bg-red-50 p-3.5 border border-red-200">
                <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-red-600 text-xs font-bold text-white">
                  !
                </span>
                <p className="text-xs text-red-900 leading-relaxed font-medium">
                  This member will be removed from search and their profile suspended.
                </p>
              </div>
            )}

            {statusError && (
              <p className="rounded bg-red-50 p-2 text-xs text-red-600">{statusError}</p>
            )}

            <form onSubmit={handleStatusSubmit} className="space-y-4">
              <div>
                <label className="label">Reason for status change (required)</label>
                <textarea
                  rows={3}
                  required
                  maxLength={500}
                  value={statusDialog.reason}
                  onChange={(e) =>
                    setStatusDialog({ ...statusDialog, reason: e.target.value })
                  }
                  placeholder="Explain why this action is being taken..."
                  className="input resize-none text-xs"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() =>
                    setStatusDialog({ open: false, member: null, targetStatus: '', reason: '' })
                  }
                  className="btn-outline"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={changingStatus}
                  className={
                    statusDialog.targetStatus === 'SUSPENDED'
                      ? 'rounded-lg bg-red-600 px-5 py-2 text-sm font-semibold text-white hover:bg-red-700'
                      : 'btn-primary'
                  }
                  style={statusDialog.targetStatus !== 'SUSPENDED' ? { width: 'auto', paddingInline: 20 } : {}}
                >
                  {changingStatus
                    ? 'Updating...'
                    : statusDialog.targetStatus === 'SUSPENDED'
                      ? 'Remove'
                      : 'Confirm'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

