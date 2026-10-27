import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'

const RELIGIONS = ['Hindu', 'Muslim', 'Christian', 'Sikh', 'Jain', 'Buddhist', 'Other']
const LANGUAGES = [
  'Hindi', 'English', 'Marathi', 'Gujarati', 'Bengali', 'Tamil',
  'Telugu', 'Kannada', 'Malayalam', 'Punjabi', 'Urdu', 'Other',
]
const STEPS = ['Basic', 'Education', 'Family']
const TITLES = [
  ['Basic details', 'Tell us about yourself.'],
  ['Education & work', 'Your studies and profession.'],
  ['Family & about', 'A few words about you and your family.'],
]

const emptyForm = {
  maritalStatus: '',
  religion: '',
  motherTongue: '',
  city: '',
  heightCm: '',
  education: '',
  occupation: '',
  familyType: '',
  about: '',
}

export default function Profile() {
  const [form, setForm] = useState(emptyForm)
  const [step, setStep] = useState(1)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [info, setInfo] = useState(null) // the profile from the server
  const [saved, setSaved] = useState(false)

  const fillForm = (p) =>
    setForm({
      maritalStatus: p.maritalStatus ?? '',
      religion: p.religion ?? '',
      motherTongue: p.motherTongue ?? '',
      city: p.city ?? '',
      heightCm: p.heightCm ?? '',
      education: p.education ?? '',
      occupation: p.occupation ?? '',
      familyType: p.familyType ?? '',
      about: p.about ?? '',
    })

  // Load the existing profile (a 404 just means "not created yet")
  useEffect(() => {
    api
      .get('/api/profiles/me')
      .then((res) => {
        setInfo(res.data)
        fillForm(res.data)
      })
      .catch((err) => {
        if (err.response?.status !== 404) setError(getErrorMessage(err))
      })
      .finally(() => setLoading(false))
  }, [])

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (step < 3) {
      setStep(step + 1)
      return
    }
    setError('')
    setSaving(true)
    try {
      const body = { ...form, heightCm: form.heightCm === '' ? null : Number(form.heightCm) }
      const res = await api.put('/api/profiles/me', body)
      setInfo(res.data)
      fillForm(res.data)
      setSaved(true)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <p className="text-sm text-muted">Loading...</p>

  if (saved) {
    return (
      <div className="card mx-auto max-w-md p-8 text-center">
        <span className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-ok text-xl text-white">
          ✓
        </span>
        <h1 className="mt-4 text-2xl font-bold">Profile saved</h1>
        <p className="mt-2 text-sm text-muted">
          Your profile is{' '}
          <span className="font-semibold text-ink">{info?.completeness ?? 0}%</span> complete.
        </p>
        <Link to="/app/search" className="btn-primary mt-6 block text-center">
          Go to search
        </Link>
        <button
          onClick={() => {
            setSaved(false)
            setStep(1)
          }}
          className="mt-3 text-xs font-semibold text-brand"
        >
          Edit profile again
        </button>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-2xl">
      {/* Stepper */}
      <div className="mb-6 flex items-center justify-center gap-6">
        {STEPS.map((label, i) => {
          const n = i + 1
          const active = n === step
          const done = n < step
          return (
            <div key={label} className="flex flex-col items-center gap-1">
              <span
                className={`flex h-8 w-8 items-center justify-center rounded-full text-sm font-semibold ${
                  active || done ? 'bg-brand text-white' : 'bg-sand text-muted'
                }`}
              >
                {done ? '✓' : n}
              </span>
              <span className={`text-xs ${active ? 'font-semibold text-brand' : 'text-muted'}`}>
                {label}
              </span>
            </div>
          )
        })}
      </div>

      <form onSubmit={handleSubmit} className="card p-8">
        <h1 className="text-2xl font-bold">{TITLES[step - 1][0]}</h1>
        <p className="mb-5 text-sm text-muted">
          Step {step} of 3. {TITLES[step - 1][1]}
        </p>

        {info && (
          <p className="mb-4 rounded-lg bg-brand-soft px-3 py-2 text-xs text-brand">
            {info.fullName} · profile {info.completeness}% complete
          </p>
        )}
        {!info && step === 1 && (
          <p className="mb-4 rounded-lg bg-brand-soft px-3 py-2 text-xs text-brand">
            Welcome! Please complete your profile. Your name, gender and date of birth come
            from your registration.
          </p>
        )}

        {error && (
          <p className="mb-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
        )}

        {step === 1 && (
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="label">Marital status</label>
              <select required className="input" value={form.maritalStatus} onChange={set('maritalStatus')}>
                <option value="">Select</option>
                <option value="NEVER_MARRIED">Never married</option>
                <option value="DIVORCED">Divorced</option>
                <option value="WIDOWED">Widowed</option>
              </select>
            </div>
            <div>
              <label className="label">Religion</label>
              <select className="input" value={form.religion} onChange={set('religion')}>
                <option value="">Select</option>
                {RELIGIONS.map((r) => (
                  <option key={r} value={r}>{r}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Mother tongue</label>
              <select required className="input" value={form.motherTongue} onChange={set('motherTongue')}>
                <option value="">Select</option>
                {LANGUAGES.map((l) => (
                  <option key={l} value={l}>{l}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">City</label>
              <input required className="input" placeholder="Enter your city"
                value={form.city} onChange={set('city')} />
            </div>
            <div>
              <label className="label">Height (cm)</label>
              <input type="number" min="100" max="250" className="input" placeholder="e.g. 165"
                value={form.heightCm} onChange={set('heightCm')} />
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="grid gap-4">
            <div>
              <label className="label">Education</label>
              <input required className="input" placeholder="e.g. B.Tech"
                value={form.education} onChange={set('education')} />
            </div>
            <div>
              <label className="label">Occupation</label>
              <input required className="input" placeholder="e.g. Software Engineer"
                value={form.occupation} onChange={set('occupation')} />
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="grid gap-4">
            <div>
              <label className="label">Family type</label>
              <select className="input" value={form.familyType} onChange={set('familyType')}>
                <option value="">Select</option>
                <option value="Nuclear">Nuclear</option>
                <option value="Joint">Joint</option>
              </select>
            </div>
            <div>
              <label className="label">About me</label>
              <textarea rows={5} maxLength={1000} className="input"
                placeholder="Write a few lines about yourself"
                value={form.about} onChange={set('about')} />
              <p className="mt-1 text-right text-xs text-muted">{form.about.length}/1000</p>
            </div>
          </div>
        )}

        <div className="mt-8 flex justify-between">
          {step > 1 ? (
            <button type="button" onClick={() => setStep(step - 1)} className="btn-outline">
              Back
            </button>
          ) : (
            <span />
          )}
          <button type="submit" disabled={saving} className="btn-primary" style={{ width: 'auto', paddingInline: 32 }}>
            {step < 3 ? 'Next' : saving ? 'Saving...' : 'Save profile'}
          </button>
        </div>
      </form>
    </div>
  )
}