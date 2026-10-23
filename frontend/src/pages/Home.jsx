import { useEffect, useState } from 'react'
import api from '../api/axios'

export default function Home() {
  const [status, setStatus] = useState('checking...')

  useEffect(() => {
    api.get('/actuator/health')
      .then((res) => setStatus(res.data.status))
      .catch(() => setStatus('DOWN (is the backend running?)'))
  }, [])

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-rose-50">
      <h1 className="text-4xl font-bold text-rose-600">Milan</h1>
      <p className="mt-2 text-gray-600">Trust-First Matrimonial Platform</p>
      <p className="mt-6 rounded bg-white px-4 py-2 shadow">
        Backend status: <span className="font-semibold">{status}</span>
      </p>
    </div>
  )
}