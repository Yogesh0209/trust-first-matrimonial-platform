# TrustMatch — Trust-First Matrimonial Platform (Frontend)

TrustMatch is a modern, trust-first matrimonial SaaS platform connecting verified candidates through registered marriage bureaus. The application features tiered trust verification (Level 1 Phone/Email OTP, Level 2 Live Selfie matching, and Level 3 Government-issued Photo ID verification).

---

## 1. Tech Stack

* **Core**: React 19 + Vite 8
* **Routing**: React Router v7 (`react-router-dom`)
* **Styling**: Tailwind CSS v4 (`@tailwindcss/vite`) with custom typography (Playfair Display + Inter) and brand design tokens
* **HTTP Client**: Axios with Bearer token authentication and 401 interceptors
* **Linting & Code Quality**: Oxlint

---

## 2. Getting Started

### Prerequisites
* Node.js 18+ (tested with Node 20+)
* Backend running at `http://localhost:8080`

### Setup and Development
```bash
# 1. Install dependencies
npm install

# 2. Configure environment
# Copy .env.example to .env:
# cp .env.example .env (or copy .env.example .env on Windows)
# Ensure .env contains:
# VITE_API_URL=http://localhost:8080

# 3. Start development server
npm run dev

# Opens at http://localhost:5173
```

### Production Build & Linting
```bash
# Check linting rules
npm run lint

# Build for production
npm run build

# Preview production build locally
npm run preview
```

---

## 3. Platform Route Table

| Route | Role / Access | Description |
|---|---|---|
| `/` | Public (Logged out) / Redirect | Landing page with hero, verification tiers, bureau features, and search demo |
| `/login` | Public | Authentication screen for Candidates, Hosts, and Super Admins |
| `/register` | Public | Candidate registration (triggers email & phone OTP verification) |
| `/verify` | Public | Multi-step OTP code confirmation |
| `/forgot-password` | Public | Account recovery OTP request |
| `/reset-password` | Public | Password reset with verified OTP |
| `/host-register` | Public | Marriage bureau host onboarding |
| `/status` | Public | Spring Boot Actuator backend health check status |
| `/app/search` | `END_USER` (Candidate) | Search with multi-attribute filters, shortlist toggle, and interest dispatches |
| `/app/profiles/:id` | `END_USER` | Detailed candidate profile with carousel, trust badges, block, and report modal |
| `/app/interests` | `END_USER` | Received, Sent, and Shortlisted interest tabs with Accept, Decline, and Withdraw |
| `/app/chat` / `/app/chat/:interestId` | `END_USER` | Real-time candidate messaging with anti-scam alerts and 10s polling |
| `/app/plans` | `END_USER` | Bureau subscription plans guide and bureau liaison |
| `/app/settings` | `END_USER` | Change password, profile visibility (`VISIBLE`/`HIDDEN`), network sharing, and search status |
| `/app/inactive` | `END_USER` | Inactive profile reactivate screen |
| `/app/payment/success` | `END_USER` | Demo payment confirmation screen |
| `/app/payment/failed` | `END_USER` | Demo payment failure screen |
| `/app/profile` | `END_USER` | 3-step candidate profile editor (Basic, Education & Work, Family & About) |
| `/app/photos` | `END_USER` | Candidate photo gallery management (add, primary, delete) |
| `/app/verification` | `END_USER` | Selfie photo and Government ID submission portal |
| `/host` | `HOST` (Bureau) | Bureau dashboard: quota usage, join trends, trust levels, and pending verifications |
| `/host/members` | `HOST` | Member management (Pause, Suspend with reason) and split-screen identity verification |
| `/host/reports` | `HOST` | Bureau safety reports with Warn, Remove, and Dismiss actions |
| `/host/billing` | `HOST` | Active plan status, upgrade requests, cross-bureau sharing toggles, and audit logs |
| `/admin` | `SUPER_ADMIN` | Global platform metrics, plan distribution, and automated maintenance jobs |
| `/admin/hosts` | `SUPER_ADMIN` | Bureau approvals, suspensions, and plan assignments |
| `/admin/plans` | `SUPER_ADMIN` | Subscription plan management (add and edit tiers) |
| `/admin/reports` | `SUPER_ADMIN` | Platform-wide safety and violation reports |
| `/admin/audit` | `SUPER_ADMIN` | Immutable security audit log |
| `*` | Any | 404 Page Not Found screen |

---

## 4. Test Accounts (Dev Database)

| Role | Email | Password | Details |
|---|---|---|---|
| **Super Admin** | `admin@milan.com` | `Admin@1234` | Full platform administrative access |
| **Host 1** | `host@test.com` | `Host@5678` | Test Bureau (Code: `TB001`) |
| **Host 2** | `host2@test.com` | `Host@1234` | Second Bureau |
| **Candidate 1** | `priya@test.com` | `Priya@1234` | Active candidate |
| **Candidate 2** | `rahul@test.com` | `Rahul@1234` | Active candidate |

---

## 5. Security & Error Handling

* **Global Error Boundary**: Unhandled render exceptions display a friendly 500 error screen with reload and home recovery.
* **404 Routing**: Unknown routes display the 404 Page Not Found screen.
* **Auth Guard**: Role-based routing automatically redirects unauthorized users to their designated role dashboard or `/login`.
