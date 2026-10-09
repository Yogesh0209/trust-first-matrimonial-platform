# Trust-First Matrimonial SaaS Platform

A multi-tenant matrimonial platform for marriage bureaus that fights fake,
inactive and scammer profiles using 3-level trust verification.

## Tech stack
React | Spring Boot | MySQL | JWT

## Roles
Super Admin, Host (marriage bureau), End User

## Branches
- `main`: stable, demo-ready code
- `dev`: integration branch
- `feature/*`: one branch per task

## Run locally
## Run the backend locally

**You need:** Java 21, MySQL 8, Maven (or the included `mvnw`).

1. Create the database and user in MySQL:
```sql
CREATE DATABASE tfmsp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'tfmsp_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON tfmsp_db.* TO 'tfmsp_user'@'localhost';
FLUSH PRIVILEGES;
```
2. Set the environment variable `DB_PASSWORD` to the password you chose.
3. Start the app from `backend/`: `mvnw spring-boot:run` (or run `BackendApplication` in STS).
4. Flyway creates all tables on first start. Check http://localhost:8080/actuator/health shows `UP`.