export function activeLabel(dateStr) {
  if (!dateStr) return { text: 'Not active recently', fresh: false }
  const mins = (Date.now() - new Date(dateStr).getTime()) / 60000
  const hours = Math.floor(mins / 60)
  const days = Math.floor(hours / 24)

  if (mins < 60) return { text: 'Active just now', fresh: true }
  if (hours < 24) return { text: `Active ${hours} hour${hours === 1 ? '' : 's'} ago`, fresh: true }
  if (days < 7) return { text: `Active ${days} day${days === 1 ? '' : 's'} ago`, fresh: true }
  if (days < 30) {
    const weeks = Math.floor(days / 7)
    return { text: `Active ${weeks} week${weeks === 1 ? '' : 's'} ago`, fresh: false }
  }
  return { text: 'Active over a month ago', fresh: false }
}