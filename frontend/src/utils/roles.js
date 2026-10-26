export function homeFor(role) {
  if (role === 'SUPER_ADMIN') return '/admin'
  if (role === 'HOST') return '/host'
  return '/app'
}