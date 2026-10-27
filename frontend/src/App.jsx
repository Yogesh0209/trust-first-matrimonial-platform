import { Routes, Route, Navigate } from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import VerifyOtp from './pages/VerifyOtp'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import HostRegister from './pages/HostRegister'
import ComingSoon from './pages/ComingSoon'
import ProtectedRoute from './routes/ProtectedRoute'
import UserLayout from './components/UserLayout'
import SidebarLayout from './components/SidebarLayout'
import { useAuth } from './context/AuthContext'
import { homeFor } from './utils/roles'
import Profile from './pages/Profile'

const hostItems = [
  { to: '/host', label: 'Dashboard', end: true },
  { to: '/host/members', label: 'Members & Verification' },
  { to: '/host/reports', label: 'Reported Profiles' },
  { to: '/host/billing', label: 'Billing & Sharing' },
]

const adminItems = [
  { to: '/admin', label: 'Dashboard', end: true },
  { to: '/admin/hosts', label: 'Hosts' },
  { to: '/admin/plans', label: 'Subscription Plans' },
]

function RootRedirect() {
  const { token, role } = useAuth()
  return <Navigate to={token ? homeFor(role) : '/login'} replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<RootRedirect />} />
      <Route path="/status" element={<Home />} />

      {/* Public screens */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/verify" element={<VerifyOtp />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/host-register" element={<HostRegister />} />

      {/* Candidate */}
      <Route
        element={
          <ProtectedRoute roles={['END_USER']}>
            <UserLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/app" element={<Navigate to="/app/search" replace />} />
        <Route path="/app/search" element={<ComingSoon title="Search" />} />
        <Route path="/app/interests" element={<ComingSoon title="Interests" />} />
        <Route path="/app/plans" element={<ComingSoon title="Plans" />} />
        <Route path="/app/verification" element={<ComingSoon title="Verification" />} />
        <Route path="/app/profile" element={<Profile />} />
      </Route>

      {/* Host */}
      <Route
        element={
          <ProtectedRoute roles={['HOST']}>
            <SidebarLayout badge="Host" items={hostItems} />
          </ProtectedRoute>
        }
      >
        <Route path="/host" element={<ComingSoon title="Dashboard" />} />
        <Route path="/host/members" element={<ComingSoon title="Members & Verification" />} />
        <Route path="/host/reports" element={<ComingSoon title="Reported Profiles" />} />
        <Route path="/host/billing" element={<ComingSoon title="Billing & Sharing" />} />
      </Route>

      {/* Super Admin */}
      <Route
        element={
          <ProtectedRoute roles={['SUPER_ADMIN']}>
            <SidebarLayout badge="Super Admin" items={adminItems} />
          </ProtectedRoute>
        }
      >
        <Route path="/admin" element={<ComingSoon title="Platform overview" />} />
        <Route path="/admin/hosts" element={<ComingSoon title="Hosts" />} />
        <Route path="/admin/plans" element={<ComingSoon title="Subscription Plans" />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}