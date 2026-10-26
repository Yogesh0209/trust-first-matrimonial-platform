import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import AuthLayout from '../components/AuthLayout'
import { getErrorMessage } from '../utils/errors'
import PasswordInput from '../components/PasswordInput'

export default function Register() {
    const navigate = useNavigate()
    const [form, setForm] = useState({
        fullName: '',
        email: '',
        phone: '',
        password: '',
        gender: 'FEMALE',
        dateOfBirth: '',
        bureauCode: '',
    })
    const [agree, setAgree] = useState(false)
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const set = (key) => (e) => setForm({ ...form, [key]: e.target.value })

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')
        setLoading(true)
        try {
            await api.post('/api/auth/register', form)
            try {
                await api.post('/api/auth/resend-otp', {
                    identifier: form.email,
                    purpose: 'EMAIL_VERIFY',
                })
            } catch {
                // the verify page has a Resend button, so we ignore this here
            }
            navigate('/verify', { state: { email: form.email, phone: form.phone } })
        } catch (err) {
            setError(getErrorMessage(err))
        } finally {
            setLoading(false)
        }
    }

    return (
        <AuthLayout>
            <form onSubmit={handleSubmit} className="card w-full max-w-md p-8">
                <h1 className="text-2xl font-bold">Create your account</h1>
                <p className="mb-5 text-sm text-muted">It takes less than two minutes.</p>

                <div className="mb-5 grid grid-cols-2 rounded-xl bg-sand p-1 text-center text-sm font-semibold">
                    <span className="rounded-lg bg-white py-2 text-brand shadow-sm">Register</span>
                    <Link to="/login" className="rounded-lg py-2 text-muted">Login</Link>
                </div>

                {error && (
                    <p className="mb-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
                )}

                <label className="label">Full name</label>
                <input required className="input mb-3" placeholder="Enter your full name"
                    value={form.fullName} onChange={set('fullName')} />

                <label className="label">Email</label>
                <input required type="email" className="input mb-3" placeholder="name@example.com"
                    value={form.email} onChange={set('email')} />

                <label className="label">Phone number</label>
                <input required className="input mb-3" placeholder="10-digit mobile number"
                    value={form.phone} onChange={set('phone')} />

                <label className="label">Password</label>
                <PasswordInput required placeholder="Minimum 8 characters" value={form.password}
                    onChange={set('password')} className="mb-1" />
                <p className="mb-3 text-xs text-muted">
                    Use uppercase, lowercase, a digit and one of @ $ ! % * ? &amp;
                </p>

                <div className="mb-3 grid grid-cols-2 gap-3">
                    <div>
                        <label className="label">Gender</label>
                        <select className="input" value={form.gender} onChange={set('gender')}>
                            <option value="FEMALE">Female</option>
                            <option value="MALE">Male</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </div>
                    <div>
                        <label className="label">Date of birth</label>
                        <input required type="date" className="input"
                            value={form.dateOfBirth} onChange={set('dateOfBirth')} />
                    </div>
                </div>

                <label className="label">Bureau code</label>
                <input required className="input mb-1" placeholder="e.g. TB001"
                    value={form.bureauCode} onChange={set('bureauCode')} />
                <p className="mt-3 text-center text-xs font-semibold text-brand">
                    <Link to="/host-register">Are you a marriage bureau? Register as a Host</Link>
                </p>
                <label className="mb-5 flex items-center gap-2 text-xs">
                    <input type="checkbox" checked={agree} onChange={(e) => setAgree(e.target.checked)}
                        className="accent-brand" />
                    I agree to the Terms and Privacy Policy
                </label>

                <button type="submit" disabled={loading || !agree} className="btn-primary">
                    {loading ? 'Creating account...' : 'Create account'}
                </button>

                <p className="mt-5 text-center text-xs text-muted">
                    Already have an account?{' '}
                    <Link to="/login" className="font-semibold text-brand">Login</Link>
                </p>
            </form>
        </AuthLayout>
    )
}