import { Routes, Route, Navigate } from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import ProtectedRoute from './routes/ProtectedRoute'
import { useAuth } from './context/AuthContext'
import { homeFor } from './utils/roles'
import Register from './pages/Register'
import VerifyOtp from './pages/VerifyOtp'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import HostRegister from './pages/HostRegister'

function RootRedirect() {
  const { token, role } = useAuth()
  return <Navigate to={token ? homeFor(role) : '/login'} replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<RootRedirect />} />
      <Route path="/status" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/verify" element={<VerifyOtp />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/host-register" element={<HostRegister />} />
      <Route
        path="/app"
        element={<ProtectedRoute roles={['END_USER']}><Dashboard /></ProtectedRoute>}
      />
      <Route
        path="/host"
        element={<ProtectedRoute roles={['HOST']}><Dashboard /></ProtectedRoute>}
      />
      <Route
        path="/admin"
        element={<ProtectedRoute roles={['SUPER_ADMIN']}><Dashboard /></ProtectedRoute>}
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}