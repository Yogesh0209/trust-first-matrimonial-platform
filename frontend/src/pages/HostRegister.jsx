import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import AuthLayout from '../components/AuthLayout'
import PasswordInput from '../components/PasswordInput'
import { getErrorMessage } from '../utils/errors'

export default function HostRegister() {
  const [form, setForm] = useState({
    bureauName: '',
    bureauCode: '',
    city: '',
    hostFullName: '',
    email: '',
    phone: '',
    password: '',
    planName: 'Basic',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [done, setDone] = useState(false)

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.post('/api/tenants/register', form)
      setDone(true)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  if (done) {
    return (
      <AuthLayout>
        <div className="card w-full max-w-md p-8 text-center">
          <span className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-ok text-xl text-white">
            ✓
          </span>
          <h1 className="mt-4 text-2xl font-bold">Registration submitted</h1>
          <p className="mt-2 text-sm text-muted">
            Your bureau <span className="font-semibold text-ink">{form.bureauName}</span> is
            waiting for approval from the platform admin. You can log in once it is approved.
          </p>
          <Link to="/login" className="btn-primary mt-6 block text-center">Go to login</Link>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout>
      <form onSubmit={handleSubmit} className="card w-full max-w-md p-8">
        <h1 className="text-2xl font-bold">Register your bureau</h1>
        <p className="mb-5 text-sm text-muted">
          Create a host account for your marriage bureau.
        </p>

        {error && (
          <p className="mb-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
        )}

        <label className="label">Bureau name</label>
        <input required className="input mb-3" placeholder="e.g. Sharma Marriage Bureau"
          value={form.bureauName} onChange={set('bureauName')} />

        <div className="mb-3 grid grid-cols-2 gap-3">
          <div>
            <label className="label">Bureau code</label>
            <input required className="input" placeholder="3-20 characters"
              value={form.bureauCode} onChange={set('bureauCode')} />
          </div>
          <div>
            <label className="label">City</label>
            <input required className="input" placeholder="e.g. Bhopal"
              value={form.city} onChange={set('city')} />
          </div>
        </div>

        <label className="label">Your full name</label>
        <input required className="input mb-3" placeholder="Host full name"
          value={form.hostFullName} onChange={set('hostFullName')} />

        <label className="label">Email</label>
        <input required type="email" className="input mb-3" placeholder="name@example.com"
          value={form.email} onChange={set('email')} />

        <label className="label">Phone number</label>
        <input required className="input mb-3" placeholder="10-digit mobile number"
          value={form.phone} onChange={set('phone')} />

        <label className="label">Password</label>
        <PasswordInput required placeholder="Minimum 8 characters"
          value={form.password} onChange={set('password')} className="mb-3" />

        <label className="label">Plan</label>
        <select className="input mb-5" value={form.planName} onChange={set('planName')}>
          <option value="Basic">Basic (up to 50 members)</option>
          <option value="Pro">Pro (up to 500 members)</option>
          <option value="Enterprise">Enterprise (unlimited)</option>
        </select>

        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? 'Submitting...' : 'Register bureau'}
        </button>

        <p className="mt-5 text-center text-xs text-muted">
          Already registered?{' '}
          <Link to="/login" className="font-semibold text-brand">Login</Link>
        </p>
      </form>
    </AuthLayout>
  )
}