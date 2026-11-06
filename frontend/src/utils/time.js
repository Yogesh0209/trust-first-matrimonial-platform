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

export function timeAgo(dateStr) {
  if (!dateStr) return ''
  const mins = Math.floor((Date.now() - new Date(dateStr).getTime()) / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins} minute${mins === 1 ? '' : 's'} ago`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours} hour${hours === 1 ? '' : 's'} ago`
  const days = Math.floor(hours / 24)
  if (days === 1) return 'yesterday'
  if (days < 7) return `${days} days ago`
  if (days < 30) {
    const weeks = Math.floor(days / 7)
    return `${weeks} week${weeks === 1 ? '' : 's'} ago`
  }
  return new Date(dateStr).toLocaleDateString()
}