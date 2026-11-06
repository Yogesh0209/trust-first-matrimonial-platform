import { useEffect, useState } from 'react'
import api from '../api/axios'
import { resolveMediaUrl } from '../utils/media'

// Shows an image. Private documents need the login token, so they are fetched as a blob first.
// Use key={src} where you render it so it resets when the image changes.
export default function SecureImage({ src, alt = '', className, fallback = null }) {
  const [blobUrl, setBlobUrl] = useState('')
  const [failed, setFailed] = useState(false)
  const isPrivate = !!src && src.startsWith('/api/files/documents/')

  useEffect(() => {
    if (!isPrivate) return undefined
    let objectUrl = ''
    let cancelled = false
    api
      .get(src, { responseType: 'blob' })
      .then((res) => {
        if (cancelled) return
        objectUrl = URL.createObjectURL(res.data)
        setBlobUrl(objectUrl)
      })
      .catch(() => {
        if (!cancelled) setFailed(true)
      })
    return () => {
      cancelled = true
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [src, isPrivate])

  if (!src || failed) return fallback
  if (isPrivate) {
    return blobUrl ? (
      <img src={blobUrl} alt={alt} className={className} />
    ) : (
      <span className="text-xs text-muted">Loading...</span>
    )
  }
  return <img src={resolveMediaUrl(src)} alt={alt} className={className} onError={() => setFailed(true)} />
}