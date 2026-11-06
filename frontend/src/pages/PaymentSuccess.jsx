import { Link } from 'react-router-dom'

const TODAY_STR = new Intl.DateTimeFormat('en-GB', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
}).format(new Date())

export default function PaymentSuccess() {
  return (
    <div className="mx-auto max-w-lg py-12">
      <div className="card p-8 sm:p-10 text-center">
        {/* Green check icon */}
        <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-green-100 text-ok">
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
            <polyline points="20 6 9 17 4 12" />
          </svg>
        </div>

        <h1 className="mt-5 text-2xl font-bold text-ink sm:text-3xl">Payment successful</h1>
        <p className="mt-1 text-sm text-muted">Your plan is now active.</p>

        {/* Receipt table */}
        <div className="mt-8 divide-y divide-line rounded-xl border border-line bg-surface text-left text-sm">
          <div className="flex justify-between px-4 py-3">
            <span className="text-muted">Plan</span>
            <span className="font-semibold text-ink">Pro</span>
          </div>
          <div className="flex justify-between px-4 py-3">
            <span className="text-muted">Amount</span>
            <span className="font-semibold text-ink">Rs 499</span>
          </div>
          <div className="flex justify-between px-4 py-3">
            <span className="text-muted">Date</span>
            <span className="font-semibold text-ink">{TODAY_STR}</span>
          </div>
          <div className="flex justify-between px-4 py-3">
            <span className="text-muted">Transaction ID</span>
            <span className="font-mono text-xs font-semibold text-ink">TXN000123</span>
          </div>
        </div>

        {/* Action buttons */}
        <div className="mt-8 flex flex-col gap-3 sm:flex-row">
          <Link to="/app/search" className="btn-primary flex-1 text-center">
            Go to dashboard
          </Link>
          <button
            type="button"
            onClick={() => alert('Demo receipt: In production, a PDF invoice is generated here.')}
            className="btn-outline flex-1 text-center"
          >
            Download receipt
          </button>
        </div>

        <p className="mt-6 text-xs text-muted">
          Demo payment screen — no actual charges were processed.
        </p>
      </div>
    </div>
  )
}
