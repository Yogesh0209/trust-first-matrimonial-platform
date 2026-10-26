import Logo from './Logo'

export default function AuthLayout({ children }) {
  return (
    <div className="grid min-h-screen lg:grid-cols-[44%_56%]">
      <aside className="hidden flex-col justify-between bg-brand p-12 text-white lg:flex">
        <Logo light />
        <div>
          <h2 className="text-4xl font-bold leading-tight">
            Every profile verified.
            <br />
            <span className="text-white/70">Every connection earned.</span>
          </h2>
          <p className="mt-4 max-w-sm text-sm text-white/70">
            Join a matrimonial platform where trust levels are visible on every
            profile and inactive accounts are removed.
          </p>
          <ul className="mt-8 space-y-3 text-sm">
            <li>✓ Selfie and Government ID verification</li>
            <li>✓ Profiles hidden after inactivity</li>
            <li>✓ Reports reviewed by registered bureaus</li>
          </ul>
        </div>
        <div />
      </aside>

      <main className="flex items-center justify-center px-4 py-10">{children}</main>
    </div>
  )
}