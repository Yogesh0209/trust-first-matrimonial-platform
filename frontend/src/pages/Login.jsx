import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { homeFor } from '../utils/roles'
import AuthLayout from '../components/AuthLayout'
import PasswordInput from '../components/PasswordInput'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const role = await login(email, password)
      navigate(homeFor(role), { replace: true })
    } catch (err) {
      setError(err.response?.data?.message || 'Cannot reach the server')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout>
      <form onSubmit={handleSubmit} className="card w-full max-w-md p-8">
        <h1 className="text-2xl font-bold">Welcome back</h1>
        <p className="mb-5 text-sm text-muted">Log in to continue.</p>

        {/* Register / Login tabs */}
        <div className="mb-5 grid grid-cols-2 rounded-xl bg-sand p-1 text-center text-sm font-semibold">
          <Link to="/register" className="rounded-lg py-2 text-muted">Register</Link>
          <span className="rounded-lg bg-white py-2 text-brand shadow-sm">Login</span>
        </div>

        {error && (
          <p className="mb-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
        )}

        <label className="label">Email</label>
        <input
          type="email"
          required
          placeholder="name@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="input mb-3"
        />

        <label className="label">Password</label>
        <PasswordInput
          required
          placeholder="Your password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="mb-2"
        />

        <div className="mb-5 text-right">
          <Link to="/forgot-password" className="text-xs font-semibold text-brand">
            Forgot password?
          </Link>
        </div>

        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? 'Logging in...' : 'Log in'}
        </button>

        <p className="mt-5 text-center text-xs text-muted">
          Don't have an account?{' '}
          <Link to="/register" className="font-semibold text-brand">Register</Link>
        </p>
        <p className="mt-3 text-center text-xs font-semibold text-brand">
          Are you a marriage bureau? Register as a Host
        </p>
      </form>
    </AuthLayout>
  )
}