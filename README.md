# TrustMatch (Milan)

A trust-first matrimonial platform. Marriage bureaus (hosts) onboard and verify their own members, so every profile you see has a visible trust level.

## Roles
| Role | What they do |
|------|--------------|
| **Super Admin** | Approves bureaus, manages subscription plans, sees platform reports and audit logs, runs scheduled jobs |
| **Host (marriage bureau)** | Adds members, reviews selfie / government ID verifications, handles reports, manages billing and bureau sharing |
| **End user (candidate)** | Builds a profile, verifies it, searches, sends interests, chats with accepted matches |

## Key features
- 3 trust levels: Level 1 email + phone OTP, Level 2 selfie, Level 3 government ID (Pro plan and above)
- Plan gating: Basic (50 members), Pro (500), Enterprise (unlimited); cross-bureau search needs Pro+
- Interests with a note (max 200 chars) that expire after 14 days; chat only after an interest is accepted, with scam-flagged messages
- Notifications, block / report / shortlist, "still looking?" inactivity check
- Search with pagination, HTML stripped from profile text, audit logs for sensitive actions

## Tech stack
- **Backend:** Java 17, Spring Boot 4.1.1, Spring Security (stateless JWT), JPA/Hibernate, Flyway, MySQL
- **Frontend:** React 19, Vite, React Router 7, Axios, Tailwind CSS v4

## Project structure
```
backend/    Spring Boot API (Flyway migrations in src/main/resources/db/migration)
frontend/   React app
```

## Run it locally

### 1. Database
Create the database and a user in MySQL:
```sql
CREATE DATABASE tfmsp_db;
CREATE USER 'tfmsp_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL ON tfmsp_db.* TO 'tfmsp_user'@'localhost';
```
Flyway creates all tables automatically on first start.

### 2. Backend (http://localhost:8080)
Set environment variables, then start:
```
DB_PASSWORD=your_password
JWT_SECRET=a_long_random_string_at_least_32_chars
```
(optional: `DB_URL`, `DB_USERNAME`)
```
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```
Health check: http://localhost:8080/actuator/health

### 3. Frontend (http://localhost:5173)
```
cd frontend
cp .env.example .env          # Windows: copy .env.example .env
npm install
npm run dev
```

### 4. Tests
```
cd backend
./mvnw test
```

## Test accounts (development data only)
| Role | Email | Password |
|------|-------|----------|
| Super Admin | admin@milan.com | Admin@1234 |
| Host | host@test.com | Host@5678 |
| Candidate | priya@test.com | Priya@1234 |
| Candidate | rahul@test.com | Rahul@1234 |

Change the default admin password before any real deployment.

## Notes
- CORS allows `http://localhost:5173` and `http://localhost:3000`.
- Verification photos are submitted as image links.
- The payment screens are demos; there is no payment gateway.
- Design vs backend differences are listed in `frontend/BACKEND_GAPS.md`.
