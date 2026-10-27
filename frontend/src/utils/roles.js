export function homeFor(role) {
  if (role === 'SUPER_ADMIN') return '/admin'
  if (role === 'HOST') return '/host'
  return '/app'
}

export function displayName(email) {
  if (!email) return 'User'
  const name = email.split('@')[0]
  return name.charAt(0).toUpperCase() + name.slice(1)
}