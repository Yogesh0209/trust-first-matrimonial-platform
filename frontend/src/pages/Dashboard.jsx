import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Dashboard() {
  const { role, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-rose-50">
      <h1 className="text-3xl font-bold text-rose-600">Welcome</h1>
      <p className="mt-2 text-gray-700">
        You are logged in as <span className="font-semibold">{role}</span>
      </p>
      <button
        onClick={handleLogout}
        className="mt-6 rounded bg-rose-600 px-4 py-2 font-semibold text-white hover:bg-rose-700"
      >
        Log out
      </button>
    </div>
  )
}