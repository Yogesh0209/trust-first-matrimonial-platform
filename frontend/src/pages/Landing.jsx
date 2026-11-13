import { Link } from 'react-router-dom'
import Logo from '../components/Logo'
import TrustBadge from '../components/TrustBadge'
import heroCoupleImg from '../assets/hero-couple.jpg'

const SAMPLE_MEMBERS = [
  {
    name: 'Priya S.',
    age: 27,
    city: 'Mumbai',
    profession: 'Software Engineer',
    level: 3,
    label: 'Level 3 Gov ID',
    image: '/images/members/priya.jpg',
    initial: 'P',
    bg: 'from-amber-100 to-rose-100',
  },
  {
    name: 'Rahul M.',
    age: 29,
    city: 'Delhi',
    profession: 'Product Manager',
    level: 3,
    label: 'Level 3 Gov ID',
    image: '/images/members/rahul.jpg',
    initial: 'R',
    bg: 'from-blue-100 to-indigo-100',
  },
  {
    name: 'Ananya K.',
    age: 25,
    city: 'Bengaluru',
    profession: 'UX Designer',
    level: 1,
    label: 'Level 1',
    image: '/images/members/ananya.jpg',
    initial: 'A',
    bg: 'from-purple-100 to-pink-100',
  },
  {
    name: 'Vikram T.',
    age: 31,
    city: 'Hyderabad',
    profession: 'Financial Analyst',
    level: 2,
    label: 'Level 2 Verified',
    image: '/images/members/vikram.jpg',
    initial: 'V',
    bg: 'from-emerald-100 to-teal-100',
  },
  {
    name: 'Meena R.',
    age: 28,
    city: 'Chennai',
    profession: 'Architect',
    level: 2,
    label: 'Level 2 Verified',
    image: '/images/members/meena.jpg',
    initial: 'M',
    bg: 'from-orange-100 to-rose-100',
  },
]

const SAMPLE_STORIES = [
  {
    quote: 'Verification gave us the confidence to connect with each other.',
    couple: 'Rohan & Pooja',
    meta: 'Illustrative story • Platform concept',
    initials: 'R & P',
  },
  {
    quote: 'Our bureau confirmed the ID, and my family felt reassured from day one.',
    couple: 'Amit & Sneha',
    meta: 'Illustrative story • Bureau verification concept',
    initials: 'A & S',
  },
  {
    quote: 'Seeing the trust level on every profile saved us a lot of time and worry.',
    couple: 'Vikram & Neha',
    meta: 'Illustrative story • Trust badge concept',
    initials: 'V & N',
  },
]

export default function Landing() {
  return (
    <div className="min-h-screen bg-surface text-ink">
      {/* Top Navbar */}
      <header className="sticky top-0 z-40 border-b border-line bg-white/95 backdrop-blur-xs">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6">
          <Logo />

          <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-muted">
            <a href="#features" className="hover:text-ink transition-colors">Features</a>
            <a href="#how-it-works" className="hover:text-ink transition-colors">How it works</a>
            <a href="#sample-members" className="hover:text-ink transition-colors">Sample Profiles</a>
            <a href="#stories" className="hover:text-ink transition-colors">Stories</a>
            <a href="#bureaus" className="hover:text-ink transition-colors">For Bureaus</a>
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
            <div className="lg:col-span-7 space-y-6 text-left">
              <span className="inline-block rounded-full bg-brand-soft px-3.5 py-1 text-xs font-semibold text-brand">
                Trust-first matrimony
              </span>
              <h1 className="text-4xl font-bold tracking-tight sm:text-5xl lg:text-6xl text-ink leading-tight">
                Find a match <br />
                <span className="text-brand">you can trust.</span>
              </h1>
              <p className="max-w-xl text-base sm:text-lg text-muted leading-relaxed">
                TrustMatch helps you discover profiles with visible verification levels and activity indicators, so you can make more informed connections.
              </p>

              <div className="flex flex-wrap items-center gap-3 pt-2">
                <Link
                  to="/register"
                  className="btn-primary text-sm sm:text-base shadow-sm"
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
              <div className="flex flex-wrap items-center gap-4 pt-3 text-xs font-medium text-muted">
                <span className="flex items-center gap-1.5">
                  <span className="flex h-4 w-4 items-center justify-center rounded-full bg-green-100 text-[10px] text-ok font-bold">✓</span>
                  Selfie-verified profiles
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="flex h-4 w-4 items-center justify-center rounded-full bg-green-100 text-[10px] text-ok font-bold">✓</span>
                  Active in last 30 days
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="flex h-4 w-4 items-center justify-center rounded-full bg-green-100 text-[10px] text-ok font-bold">✓</span>
                  Bureau-checked IDs
                </span>
              </div>
            </div>

            {/* Illustrative Hero Card Preview */}
            <div className="lg:col-span-5 flex justify-center">
              <div className="card w-full max-w-sm p-4 shadow-xl border border-line bg-white space-y-4">
                <div className="relative h-64 overflow-hidden rounded-xl bg-brand-soft">
                  <img
                    src={heroCoupleImg}
                    alt="Sample TrustMatch preview"
                    className="h-full w-full object-cover object-top"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-black/20" />
                  <div className="absolute top-3 right-3 rounded-full bg-white/95 backdrop-blur-xs px-2.5 py-1 text-[11px] font-semibold text-ok flex items-center gap-1 shadow-xs">
                    <span>✓</span> Demo: Selfie verified
                  </div>
                  <div className="absolute top-3 left-3 rounded-full bg-white/95 backdrop-blur-xs px-2.5 py-1 text-[11px] font-semibold text-brand flex items-center gap-1 shadow-xs">
                    <span>🛡️</span> Demo: ID checked
                  </div>
                  <div className="absolute bottom-3 left-3">
                    <span className="rounded-md bg-black/60 px-2 py-0.5 text-[10px] font-medium text-white backdrop-blur-xs">
                      Illustrative preview
                    </span>
                  </div>
                </div>

                <div>
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-lg font-bold text-ink">Priya S., 27</h3>
                      <p className="text-xs text-muted mt-0.5">Mumbai • Software Engineer</p>
                    </div>
                    <TrustBadge level={3} />
                  </div>
                  <div className="mt-3 flex items-center justify-between border-t border-line pt-2 text-[11px] text-muted">
                    <span className="text-ok font-medium flex items-center gap-1.5">
                      <span className="h-2 w-2 rounded-full bg-ok" /> Demo: Active today
                    </span>
                    <span>Illustrative card</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Quick Search Preview Bar */}
          <div className="mt-12 rounded-2xl bg-white border border-line p-5 sm:p-6 shadow-sm">
            <div className="mb-4 flex flex-wrap items-center justify-between gap-2 border-b border-line pb-3">
              <div>
                <h2 className="text-sm font-bold text-ink uppercase tracking-wider">Search Preview</h2>
                <p className="text-xs text-muted">Interactive preview — register to search live active profiles</p>
              </div>
              <span className="rounded-full bg-sand px-2.5 py-0.5 text-[11px] font-semibold text-muted">
                Demo filter UI
              </span>
            </div>

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
                <input type="text" placeholder="City or State" className="input text-xs" defaultValue="All India" />
              </div>
              <div className="col-span-2 md:col-span-1">
                <Link
                  to="/register"
                  className="btn-primary flex items-center justify-center text-center text-xs"
                  style={{ height: '38px' }}
                >
                  Search Matches
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
            <div className="card p-6 space-y-3 hover:shadow-md transition-shadow text-left">
              <div className="flex items-center justify-between">
                <span className="rounded-full bg-sand px-3 py-1 text-xs font-semibold text-muted">
                  Level 1
                </span>
                <span className="text-xl" aria-hidden="true">📱</span>
              </div>
              <h3 className="text-xl font-bold text-ink">Basic check</h3>
              <p className="text-xs text-muted leading-relaxed">
                Email and mobile number are confirmed with an OTP. The profile is verified as active and reachable.
              </p>
            </div>

            <div className="card p-6 space-y-3 border-2 border-brand/20 shadow-xs hover:shadow-md transition-shadow text-left">
              <div className="flex items-center justify-between">
                <span className="rounded-full bg-green-50 px-3 py-1 text-xs font-semibold text-ok">
                  Level 2 Verified
                </span>
                <span className="text-xl" aria-hidden="true">📸</span>
              </div>
              <h3 className="text-xl font-bold text-ink">Selfie check</h3>
              <p className="text-xs text-muted leading-relaxed">
                A submitted selfie photo is manually reviewed against profile photos by your marriage bureau to confirm identity.
              </p>
            </div>

            <div className="card p-6 space-y-3 hover:shadow-md transition-shadow text-left">
              <div className="flex items-center justify-between">
                <span className="rounded-full bg-brand-soft px-3 py-1 text-xs font-semibold text-brand">
                  Level 3 Gov ID
                </span>
                <span className="text-xl" aria-hidden="true">🛡️</span>
              </div>
              <h3 className="text-xl font-bold text-ink">Government ID</h3>
              <p className="text-xs text-muted leading-relaxed">
                A registered marriage bureau manually reviews and confirms a submitted government-issued photo ID document (such as Aadhaar, Passport, or PAN).
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
              { num: '1', title: 'Create your profile', desc: 'Add your basic details, preferences and photos.', icon: '👤' },
              { num: '2', title: 'Get verified', desc: 'Verify your contact details, upload a selfie, or submit a government ID.', icon: '📷' },
              { num: '3', title: 'Browse matches', desc: 'See trust levels on every profile. Filter your way.', icon: '🔍' },
              { num: '4', title: 'Connect safely', desc: 'Start conversations through private platform messaging.', icon: '💬' },
            ].map((step) => (
              <div key={step.num} className="card p-6 space-y-3 text-left hover:shadow-sm transition-shadow">
                <div className="flex items-center justify-between">
                  <span className="flex h-9 w-9 items-center justify-center rounded-full bg-brand-soft font-serif font-bold text-brand">
                    {step.num}
                  </span>
                  <span className="text-2xl" aria-hidden="true">{step.icon}</span>
                </div>
                <h3 className="text-base font-bold text-ink">{step.title}</h3>
                <p className="text-xs text-muted leading-relaxed">{step.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Meet Verified Members (Sample profiles) */}
      <section id="sample-members" className="border-t border-line bg-white py-16 px-4 sm:px-6">
        <div className="mx-auto max-w-6xl space-y-8">
          <div className="text-center max-w-2xl mx-auto space-y-2">
            <div className="inline-block rounded-full bg-sand px-3 py-1 text-xs font-semibold text-muted">
              Sample Member Profiles
            </div>
            <h2 className="text-3xl font-bold text-ink">Explore sample profiles</h2>
            <p className="text-xs text-muted leading-relaxed">
              Illustrative demo profiles for preview. Real profiles are visible upon login, each featuring genuine verification trust levels.
            </p>
          </div>

          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
            {SAMPLE_MEMBERS.map((m) => (
              <div key={m.name} className="card p-3 space-y-3 hover:shadow-md transition-all text-left">
                <div className="relative h-48 rounded-xl bg-sand/60 overflow-hidden flex items-center justify-center">
                  <img
                    src={m.image}
                    alt={`${m.name} (illustrative demo)`}
                    className="h-full w-full object-cover object-top"
                    onError={(e) => {
                      e.currentTarget.style.display = 'none'
                      const fb = e.currentTarget.parentElement?.querySelector('.fallback-avatar')
                      if (fb) fb.classList.remove('hidden')
                    }}
                  />
                  <div
                    className={`fallback-avatar hidden h-full w-full bg-gradient-to-br ${m.bg} flex flex-col items-center justify-center text-ink/70`}
                  >
                    <div className="h-16 w-16 rounded-full bg-white/80 flex items-center justify-center font-bold text-2xl text-brand shadow-xs">
                      {m.initial}
                    </div>
                    <span className="text-[11px] font-medium mt-2">{m.profession}</span>
                  </div>
                  <div className="absolute top-2 left-2">
                    <TrustBadge level={m.level} />
                  </div>
                  <div className="absolute bottom-2 right-2">
                    <span className="rounded bg-black/50 px-1.5 py-0.5 text-[9px] font-medium text-white backdrop-blur-xs">
                      Demo
                    </span>
                  </div>
                </div>

                <div>
                  <div className="flex items-center justify-between">
                    <p className="font-semibold text-sm text-ink">{m.name}</p>
                    <span className="text-[10px] text-muted">Illustrative</span>
                  </div>
                  <p className="text-xs text-muted">{m.age} yrs • {m.city}</p>
                  <p className="text-[11px] text-muted/80">{m.profession}</p>
                </div>

                <Link
                  to="/register"
                  className="btn-outline block text-center text-xs"
                  style={{ paddingBlock: 6 }}
                >
                  Connect (Sign up)
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
              View All Live Profiles
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
              { title: 'Photo control', desc: 'Manage up to 6 profile photos, select which photo is featured as primary, and update them anytime.', icon: '🖼️' },
              { title: 'Private messaging', desc: 'Chat directly on the platform with connected members without sharing your personal contact details.', icon: '💬' },
              { title: 'Profile visibility', desc: 'Pause search discovery anytime by switching your profile visibility to hidden whenever you need privacy.', icon: '👁️' },
              { title: 'Report and block', desc: 'Block unwanted members and submit reports on suspicious profiles for administrator and bureau review.', icon: '🛡️' },
            ].map((p) => (
              <div key={p.title} className="card p-6 space-y-3 hover:shadow-sm transition-shadow text-left">
                <span className="text-2xl" aria-hidden="true">{p.icon}</span>
                <h3 className="text-base font-bold text-ink">{p.title}</h3>
                <p className="text-xs text-muted leading-relaxed">{p.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Sample Member Experiences (Illustrative stories) */}
      <section id="stories" className="border-t border-line bg-white py-16 px-4 sm:px-6">
        <div className="mx-auto max-w-6xl space-y-12">
          <div className="text-center max-w-2xl mx-auto space-y-2">
            <div className="inline-block rounded-full bg-sand px-3 py-1 text-xs font-semibold text-muted">
              Illustrative Scenarios
            </div>
            <h2 className="text-3xl font-bold text-ink">Sample Member Experiences</h2>
            <p className="text-sm text-muted max-w-xl mx-auto">
              Illustrative scenarios demonstrating platform trust benefits. Genuine community stories will be highlighted as members share their wedding journeys.
            </p>
          </div>

          <div className="grid gap-6 md:grid-cols-3">
            {SAMPLE_STORIES.map((s) => (
              <div key={s.couple} className="card p-6 flex flex-col justify-between space-y-6 hover:shadow-sm transition-shadow text-left">
                <div>
                  <span className="text-3xl font-serif text-brand/40 leading-none" aria-hidden="true">“</span>
                  <p className="text-sm text-ink leading-relaxed mt-1">{s.quote}</p>
                </div>
                <div className="flex items-center gap-3 pt-3 border-t border-line">
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-brand-soft text-xs font-bold text-brand">
                    {s.initials}
                  </div>
                  <div>
                    <p className="text-sm font-bold text-ink">{s.couple}</p>
                    <p className="text-[11px] text-muted">{s.meta}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* For Marriage Bureaus Section */}
      <section id="bureaus" className="border-t border-line bg-brand text-white py-16 px-4 sm:px-6">
        <div className="mx-auto max-w-6xl grid gap-8 lg:grid-cols-12 items-center">
          <div className="lg:col-span-7 space-y-6 text-left">
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
                className="inline-block rounded-xl bg-white px-6 py-3 text-sm font-semibold text-brand hover:bg-surface transition-colors cursor-pointer"
              >
                Register as a Bureau
              </Link>
            </div>
          </div>

          {/* Bureau stats mockup */}
          <div className="lg:col-span-5">
            <div className="rounded-2xl border border-white/20 bg-brand-dark/60 p-6 space-y-4 text-left">
              <div className="flex items-center justify-between border-b border-white/10 pb-2">
                <p className="text-xs uppercase tracking-wider text-brand-soft font-semibold">
                  Bureau Dashboard Preview
                </p>
                <span className="rounded bg-white/10 px-2 py-0.5 text-[10px] text-brand-soft">
                  Demo data
                </span>
              </div>
              <p className="text-[11px] text-brand-soft/80">
                Simulated dashboard preview for demonstration purposes
              </p>
              <div className="grid grid-cols-3 gap-3 text-center">
                <div className="rounded-xl bg-white/10 p-3">
                  <span className="text-2xl font-bold">412</span>
                  <p className="text-[10px] text-brand-soft">Demo Members</p>
                </div>
                <div className="rounded-xl bg-white/10 p-3">
                  <span className="text-2xl font-bold text-warn">18</span>
                  <p className="text-[10px] text-brand-soft">Demo Pending</p>
                </div>
                <div className="rounded-xl bg-white/10 p-3">
                  <span className="text-2xl font-bold">5</span>
                  <p className="text-[10px] text-brand-soft">Demo Reports</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Start your journey with confidence banner */}
      <section className="bg-brand-dark text-white py-14 px-4 sm:px-6">
        <div className="mx-auto max-w-4xl text-center space-y-4">
          <h2 className="text-3xl font-bold sm:text-4xl text-white">Start your journey with confidence</h2>
          <p className="text-sm text-brand-soft max-w-xl mx-auto">
            Register free. Verify at your own pace. Connect when you are ready.
          </p>
          <div className="flex flex-wrap items-center justify-center gap-3 pt-3">
            <Link
              to="/register"
              className="inline-block rounded-xl bg-white px-6 py-3 text-sm font-semibold text-brand hover:bg-surface transition-colors cursor-pointer"
            >
              Create Profile
            </Link>
            <a
              href="#how-it-works"
              className="inline-block rounded-xl border border-white/40 px-6 py-3 text-sm font-semibold text-white hover:bg-white/10 transition-colors cursor-pointer"
            >
              Learn More
            </a>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer id="pricing" className="border-t border-line bg-white py-12 px-4 sm:px-6 text-xs text-muted">
        <div className="mx-auto max-w-6xl grid gap-8 sm:grid-cols-2 md:grid-cols-4">
          <div className="space-y-3 text-left">
            <Logo />
            <p className="leading-relaxed">
              A trust-first matrimonial platform connecting verified individuals through registered bureaus.
            </p>
          </div>
          <div className="text-left">
            <h4 className="font-semibold text-ink mb-2">Platform</h4>
            <ul className="space-y-1.5">
              <li><a href="#how-it-works" className="hover:text-ink">How it works</a></li>
              <li><a href="#features" className="hover:text-ink">Verification levels</a></li>
              <li><Link to="/register" className="hover:text-ink">Candidate signup</Link></li>
              <li><Link to="/host-register" className="hover:text-ink">For bureaus</Link></li>
            </ul>
          </div>
          <div className="text-left">
            <h4 className="font-semibold text-ink mb-2">Account</h4>
            <ul className="space-y-1.5">
              <li><Link to="/login" className="hover:text-ink">Login</Link></li>
              <li><Link to="/register" className="hover:text-ink">Register</Link></li>
              <li><Link to="/forgot-password" className="hover:text-ink">Reset password</Link></li>
            </ul>
          </div>
          <div className="text-left">
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
