# OrderLink

Commerce domain project with a Nuxt frontend and a Spring Boot backend.

## Stack

- Frontend: Nuxt 4, Vue 3, TypeScript, pnpm
- Backend: Spring Boot 4.1, Java 21, Gradle
- Database: PostgreSQL 17, Flyway

## Local development

Prerequisites: Node.js 24, pnpm, Java 21, and Docker.

```bash
# 1. Start PostgreSQL
docker compose up -d postgres

# 2. Start the backend
cd backend
./gradlew bootRun

# 3. Start the frontend in another terminal
cd frontend
cp .env.example .env
pnpm install
pnpm dev
```

The frontend runs at `http://localhost:3000`. The backend API runs at
`http://localhost:8080`, with health endpoints at `/api/v1/system/status` and
`/actuator/health`.

## Environment variables

Backend:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/orderlink` |
| `DB_USERNAME` | `orderlink` |
| `DB_PASSWORD` | `orderlink` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` |

Frontend:

| Variable | Default |
| --- | --- |
| `NUXT_PUBLIC_API_BASE` | `http://localhost:8080/api` |

## Verification

```bash
cd backend && ./gradlew test
cd frontend && pnpm build
```
