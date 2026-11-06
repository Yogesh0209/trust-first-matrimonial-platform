import { useEffect, useState } from 'react'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'

const FALLBACK_PLANS = [
  {
    id: 1,
    name: 'Basic',
    pricePerMonth: 0,
    memberLimit: 50,
    govIdVerification: false,
    crossBureauSearch: false,
  },
  {
    id: 2,
    name: 'Pro',
    pricePerMonth: 499,
    memberLimit: 500,
    govIdVerification: true,
    crossBureauSearch: true,
  },
  {
    id: 3,
    name: 'Enterprise',
    pricePerMonth: 999,
    memberLimit: null,
    govIdVerification: true,
    crossBureauSearch: true,
  },
]

export default function Plans() {
  const [plans, setPlans] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [infoNotice, setInfoNotice] = useState('')
  const [isFallback, setIsFallback] = useState(false)

  useEffect(() => {
    api
      .get('/api/plans')
      .then((res) => {
        setPlans(Array.isArray(res.data) ? res.data : [])
        setIsFallback(false)
      })
      .catch((err) => {
        setPlans(FALLBACK_PLANS)
        setIsFallback(true)
        if (err.response?.status !== 404) {
          setError(getErrorMessage(err))
        }
      })
      .finally(() => setLoading(false))
  }, [])

  const handleContactBureau = (planName) => {
    setInfoNotice(
      `To upgrade to the ${planName} plan or expand your membership features, please contact your registered marriage bureau administrator.`
    )
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  if (loading) {
    return <p className="text-sm text-muted">Loading plans...</p>
  }

  return (
    <div className="mx-auto max-w-5xl py-4">
      <div className="text-center">
        <h1 className="text-3xl font-bold sm:text-4xl">Choose your plan</h1>
        <p className="mt-2 text-sm text-muted">
          Selfie verification is included in every plan.
        </p>
        {isFallback && (
          <p className="mt-1 text-xs text-muted">Showing default plans</p>
        )}
      </div>

      {infoNotice && (
        <div className="mx-auto mt-6 max-w-2xl rounded-xl border border-line bg-surface p-4 text-center text-sm text-ink shadow-xs">
          <p className="font-medium text-brand">{infoNotice}</p>
        </div>
      )}

      {error && (
        <p className="mx-auto mt-6 max-w-md rounded bg-red-50 p-2 text-center text-xs text-red-600">
          {error}
        </p>
      )}

      <div className="mt-10 grid gap-6 md:grid-cols-3 items-stretch">
        {plans.map((p) => {
          const isPro = p.name?.toLowerCase() === 'pro'
          return (
            <div
              key={p.id}
              className={`relative flex flex-col justify-between rounded-2xl bg-white p-6 sm:p-8 transition ${
                isPro
                  ? 'border-2 border-brand shadow-md scale-100 md:-translate-y-2'
                  : 'card'
              }`}
            >
              {isPro && (
                <div className="absolute -top-3.5 left-1/2 -translate-x-1/2">
                  <span className="rounded-full bg-brand px-3 py-1 text-xs font-semibold text-white uppercase tracking-wider">
                    Recommended
                  </span>
                </div>
              )}

              <div>
                <h2 className="text-xl font-bold text-ink">{p.name}</h2>
                <div className="mt-4 flex items-baseline gap-1">
                  <span className="font-serif text-3xl font-bold text-ink">
                    Rs {p.pricePerMonth}
                  </span>
                  <span className="text-xs text-muted">
                    {p.pricePerMonth === 0 ? 'forever' : 'per month'}
                  </span>
                </div>

                <div className="mt-8 space-y-3 text-sm">
                  <div className="flex items-center gap-2.5 text-ink">
                    <span className="text-ok font-bold">✓</span>
                    <span>Level 2 selfie verification</span>
                  </div>

                  <div className="flex items-center gap-2.5 text-ink">
                    <span className="text-ok font-bold">✓</span>
                    <span>
                      {p.memberLimit ? `Up to ${p.memberLimit} bureau members` : 'Unlimited members'}
                    </span>
                  </div>

                  <div className="flex items-center gap-2.5">
                    {p.govIdVerification ? (
                      <>
                        <span className="text-ok font-bold">✓</span>
                        <span className="text-ink">Level 3 Government ID verification</span>
                      </>
                    ) : (
                      <>
                        <span className="text-muted/60">✕</span>
                        <span className="text-muted">Government ID verification</span>
                      </>
                    )}
                  </div>

                  <div className="flex items-center gap-2.5">
                    {p.crossBureauSearch ? (
                      <>
                        <span className="text-ok font-bold">✓</span>
                        <span className="text-ink">Cross-bureau network search</span>
                      </>
                    ) : (
                      <>
                        <span className="text-muted/60">✕</span>
                        <span className="text-muted">Cross-bureau network</span>
                      </>
                    )}
                  </div>
                </div>
              </div>

              <div className="mt-8 pt-4">
                <button
                  type="button"
                  onClick={() => handleContactBureau(p.name)}
                  className={isPro ? 'btn-primary' : 'btn-outline w-full'}
                >
                  Contact your bureau
                </button>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

