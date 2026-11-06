export default function TrustBadge({ level }) {
  if (level >= 3) {
    return (
      <span className="inline-flex items-center rounded-full bg-brand px-2 py-0.5 text-[11px] font-semibold text-white">
        ✓ Level 3 Gov ID
      </span>
    )
  }
  if (level === 2) {
    return (
      <span className="inline-flex items-center rounded-full border border-line bg-white/90 px-2 py-0.5 text-[11px] font-semibold text-brand">
        ✓ Level 2 Verified
      </span>
    )
  }
  return (
    <span className="inline-flex items-center rounded-full border border-line bg-white/90 px-2 py-0.5 text-[11px] font-semibold text-muted">
      ○ Level 1
    </span>
  )
}