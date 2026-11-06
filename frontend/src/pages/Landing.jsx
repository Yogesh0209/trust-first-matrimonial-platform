import { Link } from 'react-router-dom'
import Logo from '../components/Logo'
import TrustBadge from '../components/TrustBadge'

const SAMPLE_MEMBERS = [
  { name: 'Priya S.', age: 27, city: 'Mumbai', level: 3, label: 'Level 3 Gov ID' },
  { name: 'Rahul M.', age: 29, city: 'Delhi', level: 3, label: 'Level 3 Gov ID' },
  { name: 'Ananya K.', age: 25, city: 'Bengaluru', level: 1, label: 'Level 1' },
  { name: 'Vikram T.', age: 31, city: 'Hyderabad', level: 2, label: 'Level 2 Verified' },
  { name: 'Meena R.', age: 28, city: 'Chennai', level: 2, label: 'Level 2 Verified' },
]

export default function Landing() {
  return (
    <div className="min-h-screen bg-surface text-ink">
      {/* Top Navbar */}
      <header className="sticky top-0 z-40 border-b border-line bg-white/95 backdrop-blur-xs">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6">
          <Logo />

          <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-muted">
            <a href="#features" className="hover:text-ink">Features</a>
            <a href="#how-it-works" className="hover:text-ink">How it works</a>
            <a href="#bureaus" className="hover:text-ink">For Bureaus</a>
            <a href="#pricing" className="hover:text-ink">Pricing</a>
          </nav>

          <div className="flex items-center gap-3">
            <Link
              to="/login"
              className="btn-outline text-xs sm:text-sm"
              style={{ paddingBlock: 6, paddingInline: 14 }}
            >
              Login
            </Link>
            <Link
              to="/register"
              className="btn-primary text-xs sm:text-sm"
              style={{ width: 'auto', paddingBlock: 6, paddingInline: 16 }}
            >
              Register
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative overflow-hidden py-12 md:py-20 px-4 sm:px-6">
        <div className="mx-auto max-w-6xl">
          <div className="grid gap-12 lg:grid-cols-12 items-center">
            <div className="lg:col-span-7 space-y-6">
              <span className="inline-block rounded-full bg-brand-soft px-3 py-1 text-xs font-semibold text-brand">
                Trust-first matrimony
              </span>
              <h1 className="text-4xl font-bold tracking-tight sm:text-5xl lg:text-6xl text-ink leading-tight">
                Find a match <br />
                <span className="text-brand">you can trust.</span>
              </h1>
              <p className="max-w-xl text-base sm:text-lg text-muted leading-relaxed">
                TrustMatch connects you with verified, active profiles with clear trust levels
                so you always know who you are talking to.
              </p>

              <div className="flex flex-wrap items-center gap-3 pt-2">
                <Link
                  to="/register"
                  className="btn-primary text-sm sm:text-base"
                  style={{ width: 'auto', paddingBlock: 12, paddingInline: 28 }}
                >
                  Get Started Free
                </Link>
                <Link
                  to="/host-register"
                  className="btn-outline text-sm sm:text-base"
                  style={{ paddingBlock: 12, paddingInline: 24 }}
                >
                  I am a Marriage Bureau
                </Link>
              </div>

              {/* Trust check highlights */}
              <div className="flex flex-wrap items-center gap-4 pt-4 text-xs font-medium text-muted">
                <span className="flex items-center gap-1.5">
                  <span className="text-ok font-bold">✓</span> Selfie-verified profiles
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="text-ok font-bold">✓</span> Active in last 30 days
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="text-ok font-bold">✓</span> Bureau-checked IDs
                </span>
              </div>
            </div>

            {/* Illustrative Hero Card Mockup */}
            <div className="lg:col-span-5 flex justify-center">
              <div className="card w-full max-w-sm p-5 shadow-lg border border-line bg-white space-y-4">
                <div className="relative h-64 overflow-hidden rounded-xl bg-brand-soft flex flex-col justify-end p-4">
                  <div className="absolute top-3 right-3 rounded-full bg-white/90 backdrop-blur-xs px-2.5 py-1 text-[11px] font-semibold text-ok flex items-center gap-1">
                    <span>✓</span> Selfie verified
                  </div>
                  <div className="absolute top-3 left-3 rounded-full bg-white/90 backdrop-blur-xs px-2.5 py-1 text-[11px] font-semibold text-brand flex items-center gap-1">
                    <span>🛡️</span> ID checked by bureau
                  </div>
                  <div className="mx-auto h-32 w-32 rounded-full bg-[#dcc8cd] flex items-end justify-center overflow-hidden">
                    <div className="h-20 w-20 rounded-t-full bg-brand/30" />
                  </div>
                </div>

                <div>
                  <div className="flex items-center justify-between">
                    <h3 className="text-lg font-bold text-ink">Priya S., 27</h3>
                    <TrustBadge level={3} />
                  </div>
                  <p className="text-xs text-muted mt-0.5">Raipur • Software Engineer</p>
                  <p className="mt-2 text-[11px] text-ok font-medium flex items-center gap-1">
                    <span className="h-1.5 w-1.5 rounded-full bg-ok" /> Active today
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Quick Search Bar Simulation */}
          <div className="mt-12 rounded-2xl bg-white border border-line p-4 sm:p-6 shadow-sm">
            <div className="grid grid-cols-2 md:grid-cols-5 gap-3 items-end">
              <div>
                <label className="label">Looking for</label>
                <select className="input text-xs" defaultValue="Woman">
                  <option>Woman</option>
                  <option>Man</option>
                </select>
              </div>
              <div>
                <label className="label">Age range</label>
                <select className="input text-xs" defaultValue="21-30">
                  <option>21 - 30</option>
                  <option>31 - 40</option>
                  <option>41 - 50</option>
                </select>
              </div>
              <div>
                <label className="label">Religion</label>
                <select className="input text-xs" defaultValue="Any">
                  <option>Any</option>
                  <option>Hindu</option>
                  <option>Muslim</option>
                  <option>Christian</option>
                  <option>Sikh</option>
                  <option>Jain</option>
                </select>
              </div>
              <div>
                <label className="label">Location</label>
                <input type="text" placeholder="City or State" className="input text-xs" />
              </div>
              <div className="col-span-2 md:col-span-1">
                <Link
                  to="/register"
                  className="btn-primary flex items-center justify-center text-center text-xs"
                  style={{ height: '38px' }}
                >
                  Search
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* How we keep it real (Verification Tiers) */}
      <section id="features" className="border-t border-line bg-white py-16 px-4 sm:px-6">
        <div className="mx-auto max-w-6xl space-y-12">
          <div className="text-center max-w-2xl mx-auto space-y-2">
            <h2 className="text-3xl font-bold text-ink">How we keep it real</h2>
            <p className="text-sm text-muted">
              Every TrustMatch profile carries a visible trust level. You decide who to connect with.
            </p>
          </div>

          <div className="grid gap-6 md:grid-cols-3">
            <div className="card p-6 space-y-3">
              <span className="rounded-full bg-sand px-3 py-1 text-xs font-semibold text-muted">
                Level 1
              </span>
              <h3 className="text-xl font-bold text-ink">Basic check</h3>
              <p className="text-xs text-muted leading-relaxed">
                Email and mobile number are confirmed with an OTP. The profile is verified as active and reachable.
              </p>
            </div>

            <div className="card p-6 space-y-3 border-2 border-brand/20">
              <span className="rounded-full bg-green-50 px-3 py-1 text-xs font-semibold text-ok">
                Level 2 Verified
              </span>
              <h3 className="text-xl font-bold text-ink">Selfie check</h3>
              <p className="text-xs text-muted leading-relaxed">
                A live selfie is captured and compared with the uploaded photos to confirm the person is real.
              </p>
            </div>

            <div className="card p-6 space-y-3">
              <span className="rounded-full bg-brand-soft px-3 py-1 text-xs font-semibold text-brand">
                Level 3 Gov ID
              </span>
              <h3 className="text-xl font-bold text-ink">Government ID</h3>
              <p className="text-xs text-muted leading-relaxed">
                A registered marriage bureau physically reviews and confirms a government-issued photo ID (Aadhaar, Passport, PAN).
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* How it works */}
      <section id="how-it-works" className="py-16 px-4 sm:px-6">
        <div className="mx-auto max-w-6xl space-y-12">
          <div className="text-center max-w-2xl mx-auto space-y-2">
            <h2 className="text-3xl font-bold text-ink">How it works</h2>
            <p className="text-sm text-muted">Getting started takes just a few minutes.</p>
          </div>

          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {[
              { num: '1', title: 'Create your profile', desc: 'Add your basic details, preferences and photos.' },
              { num: '2', title: 'Get verified', desc: 'Verify your phone, take a selfie, or visit a bureau.' },
              { num: '3', title: 'Browse matches', desc: 'See trust levels on every profile. Filter your way.' },
              { num: '4', title: 'Connect safely', desc: 'Share contact details only when both sides agree.' },
            ].map((step) => (
              <div key={step.num} className="card p-6 space-y-3 text-left">
                <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-soft font-serif font-bold text-brand">
                  {step.num}
                </span>
                <h3 className="text-base font-bold text-ink">{step.title}</h3>
                <p className="text-xs text-muted leading-relaxed">{step.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Meet Verified Members (Sample profiles) */}
      <section className="border-t border-line bg-white py-16 px-4 sm:px-6">
        <div className="mx-auto max-w-6xl space-y-8">
          <div className="text-center max-w-2xl mx-auto space-y-2">
            <h2 className="text-3xl font-bold text-ink">Meet verified members</h2>
            <p className="text-xs text-muted">
              Illustrative profiles. Real profiles are shown after login, each with a verified trust level.
            </p>
          </div>

          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
            {SAMPLE_MEMBERS.map((m) => (
              <div key={m.name} className="card p-3 space-y-3">
                <div className="relative h-40 rounded-xl bg-brand-soft overflow-hidden flex items-end justify-center">
                  <div className="h-20 w-20 rounded-t-full bg-[#dcc8cd]" />
                  <div className="absolute top-2 left-2">
                    <TrustBadge level={m.level} />
                  </div>
                </div>
                <div>
                  <p className="font-semibold text-sm text-ink">{m.name}</p>
                  <p className="text-xs text-muted">{m.age} yrs • {m.city}</p>
                </div>
                <Link
                  to="/register"
                  className="btn-outline block text-center text-xs"
                  style={{ paddingBlock: 4 }}
                >
                  Send Interest
                </Link>
              </div>
            ))}
          </div>

          <div className="text-center pt-4">
            <Link
              to="/register"
              className="btn-primary"
              style={{ width: 'auto', paddingBlock: 10, paddingInline: 28 }}
            >
              View All Profiles
            </Link>
          </div>
        </div>
      </section>

      {/* Privacy and Control */}
      <section className="py-16 px-4 sm:px-6">
        <div className="mx-auto max-w-6xl space-y-12">
          <div className="text-center max-w-2xl mx-auto space-y-2">
            <h2 className="text-3xl font-bold text-ink">Your privacy, your control</h2>
            <p className="text-sm text-muted">Safety and privacy are not optional features. They are the foundation.</p>
          </div>

          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {[
              { title: 'Photo privacy', desc: 'Choose who sees your photos. Show them only to confirmed connections.' },
              { title: 'Private messaging', desc: 'Chat through the platform with real-time scam and safety protection.' },
              { title: 'Profile visibility', desc: 'Hide your profile from search anytime or set custom visibility rules.' },
              { title: 'Report and block', desc: 'Report suspicious behavior with one click. Bureau reviews happen swiftly.' },
            ].map((p) => (
              <div key={p.title} className="card p-6 space-y-2">
                <h3 className="text-base font-bold text-ink">{p.title}</h3>
                <p className="text-xs text-muted leading-relaxed">{p.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* For Marriage Bureaus Section */}
      <section id="bureaus" className="border-t border-line bg-brand text-white py-16 px-4 sm:px-6">
        <div className="mx-auto max-w-6xl grid gap-8 lg:grid-cols-12 items-center">
          <div className="lg:col-span-7 space-y-6">
            <h2 className="text-3xl font-bold sm:text-4xl text-white">For marriage bureaus</h2>
            <p className="text-sm text-brand-soft leading-relaxed max-w-xl">
              Partner with TrustMatch to reach verified seekers and manage your listings digitally.
              Offer Level 3 Government ID verification as a trusted local bureau.
            </p>
            <ul className="space-y-2.5 text-xs text-white">
              <li className="flex items-center gap-2">✓ Create and manage your verified bureau page</li>
              <li className="flex items-center gap-2">✓ Review and approve Government IDs (Level 3)</li>
              <li className="flex items-center gap-2">✓ Track members and matchmaking activity on your dashboard</li>
              <li className="flex items-center gap-2">✓ Flexible bureau plans: start free, upgrade as you grow</li>
            </ul>
            <div className="pt-2">
              <Link
                to="/host-register"
                className="inline-block rounded-xl bg-white px-6 py-3 text-sm font-semibold text-brand hover:bg-surface"
              >
                Register as a Bureau
              </Link>
            </div>
          </div>

          {/* Bureau stats mockup */}
          <div className="lg:col-span-5">
            <div className="rounded-2xl border border-white/20 bg-brand-dark/60 p-6 space-y-4">
              <p className="text-xs uppercase tracking-wider text-brand-soft font-semibold">Bureau Dashboard Preview</p>
              <div className="grid grid-cols-3 gap-3 text-center">
                <div className="rounded-xl bg-white/10 p-3">
                  <span className="text-2xl font-bold">412</span>
                  <p className="text-[10px] text-brand-soft">Members</p>
                </div>
                <div className="rounded-xl bg-white/10 p-3">
                  <span className="text-2xl font-bold text-warn">18</span>
                  <p className="text-[10px] text-brand-soft">Pending</p>
                </div>
                <div className="rounded-xl bg-white/10 p-3">
                  <span className="text-2xl font-bold">5</span>
                  <p className="text-[10px] text-brand-soft">Reports</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer id="pricing" className="border-t border-line bg-white py-12 px-4 sm:px-6 text-xs text-muted">
        <div className="mx-auto max-w-6xl grid gap-8 sm:grid-cols-2 md:grid-cols-4">
          <div className="space-y-3">
            <Logo />
            <p className="leading-relaxed">
              A trust-first matrimonial platform connecting verified individuals through registered bureaus.
            </p>
          </div>
          <div>
            <h4 className="font-semibold text-ink mb-2">Platform</h4>
            <ul className="space-y-1.5">
              <li><a href="#how-it-works" className="hover:text-ink">How it works</a></li>
              <li><a href="#features" className="hover:text-ink">Verification levels</a></li>
              <li><Link to="/register" className="hover:text-ink">Candidate signup</Link></li>
              <li><Link to="/host-register" className="hover:text-ink">For bureaus</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="font-semibold text-ink mb-2">Account</h4>
            <ul className="space-y-1.5">
              <li><Link to="/login" className="hover:text-ink">Login</Link></li>
              <li><Link to="/register" className="hover:text-ink">Register</Link></li>
              <li><Link to="/forgot-password" className="hover:text-ink">Reset password</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="font-semibold text-ink mb-2">Safety</h4>
            <ul className="space-y-1.5">
              <li>Trust & safety guidelines</li>
              <li>Privacy policy</li>
              <li>Terms of service</li>
            </ul>
          </div>
        </div>

        <div className="mx-auto max-w-6xl mt-8 border-t border-line pt-6 text-center text-[11px]">
          © 2026 TrustMatch. Trust-First Matrimonial Platform. All rights reserved.
        </div>
      </footer>
    </div>
  )
}

