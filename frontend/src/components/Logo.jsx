export default function Logo({ light = false }) {
  return (
    <div className="flex items-center gap-2">
      <span
        className={`flex h-8 w-8 items-center justify-center rounded-lg ${
          light ? 'bg-white text-brand' : 'bg-brand text-white'
        }`}
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
          <path d="M20 6 9 17l-5-5" />
        </svg>
      </span>
      <span className={`font-serif text-2xl font-bold ${light ? 'text-white' : 'text-brand'}`}>
        TrustMatch
      </span>
    </div>
  )
}onabort