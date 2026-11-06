import { Link } from 'react-router-dom'

export default function PaymentFailed() {
  return (
    <div className="mx-auto max-w-lg py-12">
      <div className="card p-8 sm:p-10 text-center">
        {/* Red cross icon */}
        <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-red-100 text-red-600">
          <svg
            width="32"
            height="32"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="3"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </div>

        <h1 className="mt-5 text-2xl font-bold text-ink sm:text-3xl">Payment failed</h1>
        <p className="mt-1 text-sm text-muted">
          Your payment did not go through.
          <br />
          Please try again or use another payment method.
        </p>

        {/* Action buttons */}
        <div className="mt-8 flex flex-col gap-3 sm:flex-row">
          <Link to="/app/plans" className="btn-primary flex-1 text-center">
            Try again
          </Link>
          <a
            href="mailto:support@milan.com"
            className="btn-outline flex-1 text-center"
          >
            Contact support
          </a>
        </div>

        <p className="mt-6 text-xs text-muted">
          Demo payment screen — no actual charges were processed.
        </p>
      </div>
    </div>
  )
}

