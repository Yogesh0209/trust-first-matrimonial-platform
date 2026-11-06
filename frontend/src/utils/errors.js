export function getErrorMessage(err) {
  const data = err.response?.data
  if (!data) return 'Cannot reach the server'
  if (Array.isArray(data.errors) && data.errors.length) {
    return data.errors.map((e) => e.message).join('. ')
  }
  return data.message || 'Something went wrong'
}