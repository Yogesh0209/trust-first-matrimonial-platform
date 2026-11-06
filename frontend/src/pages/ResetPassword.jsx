import { useEffect, useState } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import api from '../api/axios'
import CenterLayout from '../components/CenterLayout'
import OtpInput from '../components/OtpInput'
import PasswordInput from '../components/PasswordInput'
import { getErrorMessage } from '../utils/errors'

export default function ResetPassword() {
  const { state } = useLocation()
  const [otp, setOtp] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [done, setDone] = useState(false)
  const [seconds, setSeconds] = useState(60)

  useEffect(() => {
    if (seconds <= 0) return
    const t = setTimeout(() => setSeconds(seconds - 1), 1000)
    return () => clearTimeout(t)
  }, [seconds])

  if (!state?.email) return <Navigate to="/forgot-password" replace />

  const rules = [
    { label: 'At least 8 characters', ok: password.length >= 8 },
    { label: 'One number', ok: /\d/.test(password) },
    { label: 'One special character', ok: /[^A-Za-z0-9]/.test(password) },
  ]
  const rulesOk = rules.every((r) => r.ok)
  const matches = password === confirm
  const canSubmit = otp.length === 6 && rulesOk && matches && confirm !== ''

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.post('/api/auth/reset-password', {
        identifier: state.email,
        otp,
        newPassword: password,
      })
      setDone(true)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  const handleResend = async () => {
    setError('')
    try {
      await api.post('/api/auth/forgot-password', { identifier: state.email })
      setSeconds(60)
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  if (done) {
    return (
      <CenterLayout>
        <div className="text-center">
          <span className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-ok text-xl text-white">
            ✓
          </span>
          <h1 className="mt-4 text-2xl font-bold">Password updated</h1>
          <p className="mt-2 text-xs text-muted">You can now log in with your new password.</p>
          <Link to="/login" className="btn-primary mt-6 block text-center">Go to login</Link>
        </div>
      </CenterLayout>
    )
  }

  return (
    <CenterLayout>
      <form onSubmit={handleSubmit}>
        <h1 className="text-2xl font-bold">Set a new password</h1>
        <p className="mb-4 mt-1 text-xs text-muted">
          Enter the 6-digit code sent to <span className="font-semibold text-ink">{state.email}</span>
        </p>

        {error && (
          <p className="mb-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
        )}

        <OtpInput value={otp} onChange={setOtp} />
        <div className="mb-4 mt-2 text-center text-xs text-muted">
          {seconds > 0 ? (
            <>Resend code in 00:{String(seconds).padStart(2, '0')}</>
          ) : (
            <button type="button" onClick={handleResend} className="font-semibold text-brand">
              Resend code
            </button>
          )}
        </div>

        <label className="label">New password</label>
        <PasswordInput
          required
          placeholder="Enter new password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="mb-3"
        />

        <label className="label">Confirm password</label>
        <PasswordInput
          required
          placeholder="Re-enter password"
          value={confirm}
          onChange={(e) => setConfirm(e.target.value)}
          className="mb-1"
        />
        {confirm !== '' && !matches && (
          <p className="text-xs text-red-600">Passwords do not match</p>
        )}

        <ul className="my-4 space-y-1 text-xs">
          {rules.map((r) => (
            <li key={r.label} className={r.ok ? 'text-ok' : 'text-muted'}>
              {r.ok ? '✓' : '○'} {r.label}
            </li>
          ))}
        </ul>

        <button type="submit" disabled={loading || !canSubmit} className="btn-primary">
          {loading ? 'Saving...' : 'Reset password'}
        </button>

        <p className="mt-3 text-center text-[11px] text-muted">
          Development: the code is printed in the backend console.
        </p>
      </form>
    </CenterLayout>
  )
}