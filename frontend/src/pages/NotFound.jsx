import { Link } from 'react-router-dom'
import Logo from '../components/Logo'

export default function NotFound() {
  return (
    <div className="min-h-screen flex flex-col justify-between bg-surface text-ink">
      <header className="flex h-16 items-center border-b border-line bg-white px-6">
        <Logo />
      </header>

      <main className="flex flex-1 flex-col items-center justify-center p-6 text-center">
        <span className="font-serif text-8xl sm:text-9xl font-extrabold text-brand-soft select-none">
          404
        </span>

        <h1 className="mt-2 text-2xl sm:text-3xl font-bold text-ink">Page not found</h1>
        <p className="mt-2 text-sm text-muted max-w-sm">
          The page you are looking for does not exist or was moved.
        </p>

        <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
          <Link
            to="/"
            className="btn-primary"
            style={{ width: 'auto', paddingInline: 24 }}
          >
            Go to home
          </Link>
          <button
            type="button"
            onClick={() => window.history.back()}
            className="btn-outline"
          >
            Go back
          </button>
        </div>
      </main>

      <footer className="p-4 text-center text-xs text-muted border-t border-line bg-white">
        © 2026 TrustMatch. Trust-First Matrimonial Platform.
      </footer>
    </div>
  )
}

