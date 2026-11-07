# Backend Gaps and Mismatches

This document tracks features or fields in the design screens that are not supported or are partially supported by the Spring Boot backend APIs.

---

### Phase 4: Messages and Notifications
* None. All endpoints (`/api/conversations`, `/api/conversations/{interestId}/messages`, `/api/notifications`, `/api/notifications/unread-count`, etc.) exist and are integrated.

---

### Phase 5: Candidate Settings, Plans, Inactive Profile, Payment Demo
* **Plans (`/app/plans` - Design 08)**:
  * Design shows candidate-tier plans (Free / Pro / Premium with candidate features like "unlimited interests", "who viewed you", "profile highlight").
  * Backend reality: Subscription plans belong to marriage bureaus/hosts, not individual candidates (candidates cannot self-purchase subscriptions).
  * Implementation: Renders real bureau subscription plans (`Basic`, `Pro`, `Enterprise`) from `/api/plans` (with robust fallbacks) showcasing member limits, Level 3 Government ID verification, and cross-bureau matching. Action buttons are informational ("Contact your bureau").
* **Payments (Designs 22 & 23)**:
  * Candidates do not pay; plans belong to bureaus. The demo candidate payment screens were removed.
  * Bureaus pay for plans on `/host/billing` through Razorpay (test mode). The backend creates the order, verifies the signature and activates the plan. Payment history comes from `GET /api/host/payments`.

---

### Phase 6: Host Portal (`/host/*`)
* **Dashboard Analytics (`/host` - Design 09)**:
  * The backend `GET /api/host/dashboard` returns aggregated summary numbers only.
  * Historical "members joined per month" and "trust level distribution" are calculated client-side by aggregating the member dataset from `GET /api/host/members`.
* **Billing History & Cancel Subscription (`/host/billing` - Design 12)**:
  * The backend provides current plan status and renewal dates via `GET /api/host/subscription`, but provide past invoice line items or a cancellation endpoint.
  * An upgrade flow is integrated via `POST /api/host/subscription/upgrade` which submits an upgrade request for Super Admin approval.

---

### Phase 7: Super Admin Portal (`/admin/*`)
* **Revenue Analytics (`/admin` - Design 13)**:
  * Design 13 shows monthly revenue totals and historical revenue bars.
  * Backend reality: No payment/revenue tracking backend exists.
  * Implementation: Replaced the revenue chart with a real `planDistribution` distribution visualization (`Basic`, `Pro`, `Enterprise`) using data from `GET /api/admin/dashboard`. "Recently joined hosts" displays real bureaus from `GET /api/admin/tenants`.
* **Host Registration (`/admin/hosts` - Design 27)**:
  * In the platform model, marriage bureaus onboard themselves via `/host-register`.
  * The "Add host" button links to `/host-register`, while "Manage" provides Admin status approval, suspension (with required reason), and subscription plan assignment.
* **Subscription Plan Editing (`/admin/plans` - Design 14)**:
  * The plan schema supports `name`, `pricePerMonth`, `memberLimit`, `govIdVerification`, and `status`. `crossBureauSearch` is an internal backend permission tied to plan tiers rather than an editable form field in `PUT /api/admin/plans/{id}`.
