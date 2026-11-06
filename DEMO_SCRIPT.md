# TrustMatch demo script (about 5 minutes)

Open two browser windows (one normal, one private) so you can switch roles quickly. Start with backend and frontend running.

**Story to say first (15 sec):** "In most matrimonial sites anyone can create a fake profile. TrustMatch lets marriage bureaus verify their members, and shows a trust level on every profile."

## 1. Bureau joins (45 sec) - public + Super Admin
1. Open `/` (landing page), click **I am a Marriage Bureau** (`/host-register`), fill the form.
2. Log in as **admin@milan.com** > **Hosts**: the new bureau is PENDING. Open it, **Approve**.
3. Mention: admin can suspend with a required reason; everything is audited.

## 2. Plans (20 sec) - Super Admin
Open **Subscription Plans**: Basic / Pro / Enterprise. Back in **Hosts**, assign Pro to the bureau for a few months. Say: "Plan decides member limit, gov-ID verification and cross-bureau search."

## 3. Member signs up and verifies (60 sec) - Candidate
1. Log out. Register a new candidate, verify with the OTP (shown in the backend console in dev).
2. Fill the profile, add a photo.
3. Open **Verification**: submit a selfie link. Say: "Level 1 is email + phone, Level 2 selfie, Level 3 government ID."

## 4. Bureau verifies (45 sec) - Host
Log in as **host@test.com** > **Members & Verification** > Pending verification: see the selfie, tick the checklist, **Approve**. Show **Reject** needs a reason. Show **Add member**.

## 5. Search and connect (60 sec) - Candidates
1. Log in as **priya@test.com** > **Search**: filters, trust badges, pagination.
2. Open Rahul's profile > **Send interest** with a short note. Shortlist him.
3. Switch to the private window as **rahul@test.com** > **Interests** > Accept.
4. Both open **Messages**: chat works only after acceptance. Send a message containing a link (e.g. http://example.com) to show the scam flag.

## 6. Safety (30 sec)
As Priya: **Report** a profile and **Block** one. As the host: **Reported Profiles** > Warn / Remove / Dismiss with notes.

## 7. Billing and admin tools (30 sec)
Host **Billing & Sharing**: sharing toggles show the "upgrade to Pro" message on Basic. Admin **Dashboard**: run the *Inactive profile check* and *Subscription expiry check* buttons, then show **Audit Log**.

## Likely questions
- **Why bureaus?** Human verification adds trust that automated checks alone cannot.
- **Security?** Stateless JWT, role-based routes and APIs, input sanitised, audit trail.
- **What is not done?** Real payments and file upload storage (links are used), noted in BACKEND_GAPS.md.
