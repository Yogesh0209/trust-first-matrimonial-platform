import { useEffect, useState } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import api from '../api/axios'
import Logo from '../components/Logo'
import OtpInput from '../components/OtpInput'
import { getErrorMessage } from '../utils/errors'

export default function VerifyOtp() {
  const { state } = useLocation()
  const [step, setStep] = useState('email') // email -> phone -> done
  const [otp, setOtp] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [seconds, setSeconds] = useState(60)

  // countdown for the Resend button
  useEffect(() => {
    if (seconds <= 0) return
    const t = setTimeout(() => setSeconds(seconds - 1), 1000)
    return () => clearTimeout(t)
  }, [seconds])

  if (!state?.email) return <Navigate to="/register" replace />

  const isEmail = step === 'email'
  const identifier = isEmail ? state.email : state.phone

  const sendOtp = async (purpose, id) => {
    await api.post('/api/auth/resend-otp', { identifier: id, purpose })
    setSeconds(60)
  }

  const handleVerify = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.post(isEmail ? '/api/auth/verify-email' : '/api/auth/verify-phone', {
        identifier,
        otp,
      })
      setOtp('')
      if (isEmail) {
        setStep('phone')
        try {
          await sendOtp('PHONE_VERIFY', state.phone)
        } catch {
          setSeconds(0)
        }
      } else {
        setStep('done')
      }
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  const handleResend = async () => {
    setError('')
    try {
      await sendOtp(isEmail ? 'EMAIL_VERIFY' : 'PHONE_VERIFY', identifier)
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  return (
    <div className="min-h-screen border-t-4 border-brand">
      <div className="mt-10 flex justify-center"><Logo /></div>

      <div className="card mx-auto mt-6 w-full max-w-sm p-8">
        {step === 'done' ? (
          <>
            <h1 className="text-2xl font-bold">You're verified</h1>
            <p className="mt-2 text-sm text-muted">
              Your email and phone are confirmed. Your bureau activates your account;
              you can log in once it is approved.
            </p>
            <Link to="/login" className="btn-primary mt-6 block text-center">Go to login</Link>
          </>
        ) : (
          <form onSubmit={handleVerify}>
            <h1 className="text-2xl font-bold">
              {isEmail ? 'Verify your email' : 'Verify your phone'}
            </h1>
            <p className="mb-5 mt-1 text-xs text-muted">
              Enter the 6-digit code sent to
              <br />
              <span className="font-semibold text-ink">{identifier}</span>
            </p>

            {error && (
              <p className="mb-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
            )}

            <OtpInput key={step} value={otp} onChange={setOtp} />

            <button type="submit" disabled={loading || otp.length !== 6} className="btn-primary mt-6">
              {loading ? 'Verifying...' : 'Verify'}
            </button>

            <div className="mt-4 text-center text-xs text-muted">
              {seconds > 0 ? (
                <>Resend code in 00:{String(seconds).padStart(2, '0')}</>
              ) : (
                <button type="button" onClick={handleResend} className="font-semibold text-brand">
                  Resend code
                </button>
              )}
            </div>
            <p className="mt-3 text-center text-[11px] text-muted">
              Development: the code is printed in the backend console.
            </p>
          </form>
        )}
      </div>
    </div>
  )
}