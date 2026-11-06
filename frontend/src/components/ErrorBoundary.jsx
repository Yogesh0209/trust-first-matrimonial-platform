import { Component } from 'react'
import Logo from './Logo'

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error }
  }

  componentDidCatch(error, errorInfo) {
    console.error('Unhandled application error:', error, errorInfo)
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex flex-col justify-between bg-surface text-ink">
          <header className="flex h-16 items-center border-b border-line bg-white px-6">
            <Logo />
          </header>

          <main className="flex flex-1 flex-col items-center justify-center p-6 text-center">
            <span className="font-serif text-8xl sm:text-9xl font-extrabold text-brand-soft select-none">
              500
            </span>

            <h1 className="mt-2 text-2xl sm:text-3xl font-bold text-ink">Something went wrong</h1>
            <p className="mt-2 text-sm text-muted max-w-sm">
              Please try again in a few minutes.
            </p>

            <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
              <button
                type="button"
                onClick={() => window.location.reload()}
                className="btn-primary"
                style={{ width: 'auto', paddingInline: 24 }}
              >
                Try again
              </button>
              <a
                href="/"
                className="btn-outline"
              >
                Go to home
              </a>
            </div>
          </main>

          <footer className="p-4 text-center text-xs text-muted border-t border-line bg-white">
            © 2026 TrustMatch. Trust-First Matrimonial Platform.
          </footer>
        </div>
      )
    }

    return this.props.children
  }
}

