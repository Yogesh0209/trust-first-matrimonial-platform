import { useEffect, useState } from 'react'
import api from '../api/axios'
import PasswordInput from '../components/PasswordInput'
import { getErrorMessage } from '../utils/errors'

export default function Settings() {
  const [loading, setLoading] = useState(true)

  // Visibility state
  const [visibility, setVisibility] = useState('VISIBLE')
  const [savingPrivacy, setSavingPrivacy] = useState(false)
  const [privacySuccess, setPrivacySuccess] = useState('')
  const [privacyError, setPrivacyError] = useState('')

  // Sharing state
  const [consent, setConsent] = useState(false)
  const [savingSharing, setSavingSharing] = useState(false)
  const [sharingSuccess, setSharingSuccess] = useState('')
  const [sharingError, setSharingError] = useState('')

  // Still looking status
  const [stillLooking, setStillLooking] = useState('ACTIVE')
  const [savingLooking, setSavingLooking] = useState(false)
  const [lookingSuccess, setLookingSuccess] = useState('')
  const [lookingError, setLookingError] = useState('')

  // Password change state
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [savingPw, setSavingPw] = useState(false)
  const [pwSuccess, setPwSuccess] = useState('')
  const [pwError, setPwError] = useState('')

  useEffect(() => {
    api
      .get('/api/profiles/me')
      .then((res) => {
        setProfile(res.data)
        if (res.data.visibility) setVisibility(res.data.visibility)
        if (typeof res.data.sharingConsent === 'boolean') setConsent(res.data.sharingConsent)
        if (typeof res.data.shareConsent === 'boolean') setConsent(res.data.shareConsent)
        if (res.data.searchStatus) setStillLooking(res.data.searchStatus)
      })
      .catch((err) => {
        // If 404, profile not yet created
        if (err.response?.status !== 404) {
          setPrivacyError(getErrorMessage(err))
        }
      })
      .finally(() => setLoading(false))
  }, [])

  // 1. Update Profile Visibility
  const handleUpdatePrivacy = async (newVal) => {
    setVisibility(newVal)
    setSavingPrivacy(true)
    setPrivacySuccess('')
    setPrivacyError('')
    try {
      await api.put('/api/profiles/me/privacy', { visibility: newVal })
      setPrivacySuccess(`Profile visibility updated to ${newVal.toLowerCase()}.`)
    } catch (err) {
      setPrivacyError(getErrorMessage(err))
    } finally {
      setSavingPrivacy(false)
    }
  }

  // 2. Update Bureau Sharing Consent
  const handleUpdateSharing = async (e) => {
    const val = e.target.checked
    setConsent(val)
    setSavingSharing(true)
    setSharingSuccess('')
    setSharingError('')
    try {
      await api.put('/api/profiles/me/sharing', { consent: val })
      setSharingSuccess('Sharing preferences updated.')
    } catch (err) {
      setSharingError(getErrorMessage(err))
    } finally {
      setSavingSharing(false)
    }
  }

  // 3. Update Still Looking Status
  const handleUpdateStillLooking = async (e) => {
    e.preventDefault()
    setSavingLooking(true)
    setLookingSuccess('')
    setLookingError('')
    try {
      await api.post('/api/profiles/me/still-looking', { status: stillLooking })
      setLookingSuccess('Search status successfully updated.')
    } catch (err) {
      setLookingError(getErrorMessage(err))
    } finally {
      setSavingLooking(false)
    }
  }

  // 4. Change Password
  const handleChangePassword = async (e) => {
    e.preventDefault()
    setPwSuccess('')
    setPwError('')

    if (!currentPassword) {
      setPwError('Current password is required.')
      return
    }
    if (newPassword.length < 8) {
      setPwError('New password must be at least 8 characters long.')
      return
    }
    if (!/\d/.test(newPassword)) {
      setPwError('New password must include at least one number.')
      return
    }
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(newPassword)) {
      setPwError('New password must include at least one special character.')
      return
    }
    if (newPassword !== confirmPassword) {
      setPwError('New passwords do not match.')
      return
    }

    setSavingPw(true)
    try {
      await api.put('/api/auth/change-password', {
        currentPassword,
        newPassword,
      })
      setPwSuccess('Password changed successfully.')
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
    } catch (err) {
      setPwError(getErrorMessage(err))
    } finally {
      setSavingPw(false)
    }
  }

  if (loading) {
    return <p className="text-sm text-muted">Loading settings...</p>
  }

  return (
    <div className="mx-auto max-w-2xl space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-ink">Account Settings</h1>
        <p className="mt-1 text-sm text-muted">
          Manage your account security, profile visibility, and matching preferences.
        </p>
      </div>

      {/* Profile Visibility */}
      <section className="card p-6">
        <h2 className="text-xl font-bold text-ink">Profile Visibility</h2>
        <p className="mt-1 text-xs text-muted">
          Control whether your profile appears in search results to other members.
        </p>

        {privacyError && (
          <p className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{privacyError}</p>
        )}
        {privacySuccess && (
          <p className="mt-3 rounded-lg bg-green-50 px-3 py-2 text-sm text-ok">{privacySuccess}</p>
        )}

        <div className="mt-4 space-y-3">
          <label className="flex cursor-pointer items-start gap-3 rounded-lg border border-line p-3 transition hover:bg-surface">
            <input
              type="radio"
              name="visibility"
              value="VISIBLE"
              checked={visibility === 'VISIBLE'}
              onChange={() => handleUpdatePrivacy('VISIBLE')}
              disabled={savingPrivacy}
              className="mt-1 text-brand"
            />
            <div>
              <span className="text-sm font-semibold text-ink">Visible (Active)</span>
              <p className="text-xs text-muted">
                Your profile appears in search results and can receive connection requests.
              </p>
            </div>
          </label>

          <label className="flex cursor-pointer items-start gap-3 rounded-lg border border-line p-3 transition hover:bg-surface">
            <input
              type="radio"
              name="visibility"
              value="HIDDEN"
              checked={visibility === 'HIDDEN'}
              onChange={() => handleUpdatePrivacy('HIDDEN')}
              disabled={savingPrivacy}
              className="mt-1 text-brand"
            />
            <div>
              <span className="text-sm font-semibold text-ink">Hidden (Private)</span>
              <p className="text-xs text-muted">
                Your profile will be hidden from search. Existing accepted connections can still chat with you.
              </p>
            </div>
          </label>
        </div>
      </section>

      {/* Bureau Sharing Consent */}
      <section className="card p-6">
        <h2 className="text-xl font-bold text-ink">Network Sharing</h2>
        <p className="mt-1 text-xs text-muted">
          Allow partner marriage bureaus in our verified network to recommend your profile to their candidates.
        </p>

        {sharingError && (
          <p className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{sharingError}</p>
        )}
        {sharingSuccess && (
          <p className="mt-3 rounded-lg bg-green-50 px-3 py-2 text-sm text-ok">{sharingSuccess}</p>
        )}

        <div className="mt-4">
          <label className="flex cursor-pointer items-center gap-3">
            <input
              type="checkbox"
              checked={consent}
              onChange={handleUpdateSharing}
              disabled={savingSharing}
              className="h-4 w-4 rounded border-line text-brand focus:ring-brand"
            />
            <span className="text-sm font-medium text-ink">
              Allow cross-bureau matching and sharing
            </span>
          </label>
        </div>
      </section>

      {/* Still Looking Status */}
      <section className="card p-6">
        <h2 className="text-xl font-bold text-ink">Matrimonial Search Status</h2>
        <p className="mt-1 text-xs text-muted">
          Keep your matchmakers informed of your current status.
        </p>

        {lookingError && (
          <p className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{lookingError}</p>
        )}
        {lookingSuccess && (
          <p className="mt-3 rounded-lg bg-green-50 px-3 py-2 text-sm text-ok">{lookingSuccess}</p>
        )}

        <form onSubmit={handleUpdateStillLooking} className="mt-4 space-y-4">
          <div className="space-y-2">
            {[
              { id: 'ACTIVE', label: 'Actively searching for matches' },
              { id: 'ENGAGED', label: 'In serious discussions / engaged' },
              { id: 'PAUSED', label: 'Taking a temporary pause' },
            ].map((option) => (
              <label
                key={option.id}
                className="flex cursor-pointer items-center gap-3 rounded-lg border border-line p-3 hover:bg-surface"
              >
                <input
                  type="radio"
                  name="stillLooking"
                  value={option.id}
                  checked={stillLooking === option.id}
                  onChange={(e) => setStillLooking(e.target.value)}
                  className="text-brand"
                />
                <span className="text-sm text-ink">{option.label}</span>
              </label>
            ))}
          </div>

          <button
            type="submit"
            disabled={savingLooking}
            className="btn-primary"
            style={{ width: 'auto', paddingInline: 20 }}
          >
            {savingLooking ? 'Updating...' : 'Save status'}
          </button>
        </form>
      </section>

      {/* Change Password */}
      <section className="card p-6">
        <h2 className="text-xl font-bold text-ink">Change Password</h2>
        <p className="mt-1 text-xs text-muted">
          Must be at least 8 characters long with at least one number and one special character.
        </p>

        {pwError && (
          <p className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{pwError}</p>
        )}
        {pwSuccess && (
          <p className="mt-3 rounded-lg bg-green-50 px-3 py-2 text-sm text-ok">{pwSuccess}</p>
        )}

        <form onSubmit={handleChangePassword} className="mt-4 space-y-4">
          <div>
            <label className="label">Current Password</label>
            <PasswordInput
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              placeholder="Enter current password"
              required
            />
          </div>

          <div>
            <label className="label">New Password</label>
            <PasswordInput
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="Min 8 chars, 1 number, 1 symbol"
              required
            />
          </div>

          <div>
            <label className="label">Confirm New Password</label>
            <PasswordInput
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Re-enter new password"
              required
            />
          </div>

          <button
            type="submit"
            disabled={savingPw}
            className="btn-primary"
            style={{ width: 'auto', paddingInline: 24 }}
          >
            {savingPw ? 'Updating Password...' : 'Update Password'}
          </button>
        </form>
      </section>
    </div>
  )
}
