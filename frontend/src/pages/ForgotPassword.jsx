import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import CenterLayout from '../components/CenterLayout'
import { getErrorMessage } from '../utils/errors'

export default function ForgotPassword() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.post('/api/auth/forgot-password', { identifier: email.trim() })
      navigate('/reset-password', { state: { email: email.trim() } })
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <CenterLayout>
      <form onSubmit={handleSubmit}>
        <h1 className="text-2xl font-bold">Forgot password?</h1>
        <p className="mb-5 mt-1 text-xs text-muted">
          Enter your email and we will send you a code to reset your password.
        </p>

        {error && (
          <p className="mb-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
        )}

        <label className="label">Email</label>
        <input
          required
          type="email"
          className="input mb-5"
          placeholder="name@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? 'Sending...' : 'Send reset code'}
        </button>

        <p className="mt-5 text-center text-xs font-semibold text-brand">
          <Link to="/login">Back to login</Link>
        </p>
      </form>
    </CenterLayout>
  )
}