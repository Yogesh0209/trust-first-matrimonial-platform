import { Routes, Route, Navigate } from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import VerifyOtp from './pages/VerifyOtp'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import HostRegister from './pages/HostRegister'
import ProtectedRoute from './routes/ProtectedRoute'
import UserLayout from './components/UserLayout'
import SidebarLayout from './components/SidebarLayout'
import { useAuth } from './context/AuthContext'
import { homeFor } from './utils/roles'
import Profile from './pages/Profile'
import Photos from './pages/Photos'
import Verification from './pages/Verification'
import Search from './pages/Search'
import ProfileDetail from './pages/ProfileDetail'
import Interests from './pages/Interests'
import Chat from './pages/Chat'
import Plans from './pages/Plans'
import Settings from './pages/Settings'
import InactiveProfile from './pages/InactiveProfile'
import PaymentSuccess from './pages/PaymentSuccess'
import PaymentFailed from './pages/PaymentFailed'
import HostDashboard from './pages/HostDashboard'
import HostMembers from './pages/HostMembers'
import HostReports from './pages/HostReports'
import HostBilling from './pages/HostBilling'
import AdminDashboard from './pages/AdminDashboard'
import AdminHosts from './pages/AdminHosts'
import AdminPlans from './pages/AdminPlans'
import AdminReports from './pages/AdminReports'
import AdminAuditLogs from './pages/AdminAuditLogs'
import Landing from './pages/Landing'
import NotFound from './pages/NotFound'

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
  { to: '/admin/reports', label: 'Reports' },
  { to: '/admin/audit', label: 'Audit Log' },
]

function RootRedirect() {
  const { token, role } = useAuth()
  return token ? <Navigate to={homeFor(role)} replace /> : <Landing />
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
        <Route path="/app/search" element={<Search />} />
        <Route path="/app/profiles/:id" element={<ProfileDetail />} />
        <Route path="/app/interests" element={<Interests />} />
        <Route path="/app/chat" element={<Chat />} />
        <Route path="/app/chat/:interestId" element={<Chat />} />
        <Route path="/app/plans" element={<Plans />} />
        <Route path="/app/settings" element={<Settings />} />
        <Route path="/app/inactive" element={<InactiveProfile />} />
        <Route path="/app/payment/success" element={<PaymentSuccess />} />
        <Route path="/app/payment/failed" element={<PaymentFailed />} />
        <Route path="/app/verification" element={<Verification />} />
        <Route path="/app/profile" element={<Profile />} />
        <Route path="/app/photos" element={<Photos />} />
      </Route>

      {/* Host */}
      <Route
        element={
          <ProtectedRoute roles={['HOST']}>
            <SidebarLayout badge="Host" items={hostItems} />
          </ProtectedRoute>
        }
      >
        <Route path="/host" element={<HostDashboard />} />
        <Route path="/host/members" element={<HostMembers />} />
        <Route path="/host/reports" element={<HostReports />} />
        <Route path="/host/billing" element={<HostBilling />} />
      </Route>

      {/* Super Admin */}
      <Route
        element={
          <ProtectedRoute roles={['SUPER_ADMIN']}>
            <SidebarLayout badge="Super Admin" items={adminItems} />
          </ProtectedRoute>
        }
      >
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/hosts" element={<AdminHosts />} />
        <Route path="/admin/plans" element={<AdminPlans />} />
        <Route path="/admin/reports" element={<AdminReports />} />
        <Route path="/admin/audit" element={<AdminAuditLogs />} />
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}
