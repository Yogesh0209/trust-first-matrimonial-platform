import api from '../api/axios'

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAX_BYTES = 5 * 1024 * 1024

// Turn "/api/files/..." into "http://localhost:8080/api/files/..."; full links stay unchanged.
export function resolveMediaUrl(path) {
  if (!path) return ''
  if (/^https?:\/\//i.test(path)) return path
  return `${import.meta.env.VITE_API_URL || ''}${path}`
}

// Returns an error message, or '' when the file is fine.
export function checkImageFile(file) {
  if (!ALLOWED_TYPES.includes(file.type)) return 'Only JPG, PNG or WEBP images are allowed.'
  if (file.size > MAX_BYTES) return 'Image must be 5 MB or smaller.'
  return ''
}

// kind: 'photo' (public, profile photos) or 'document' (private, selfie and ID). Returns the saved URL.
export async function uploadImage(file, kind) {
  const form = new FormData()
  form.append('file', file)
  const res = await api.post(`/api/files/${kind}`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return res.data.url
}