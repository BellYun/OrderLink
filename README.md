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

## Run with Docker

The entire application can be built and started with Docker Compose.

```bash
cp .env.example .env
docker compose up --build
```

The frontend is available at `http://localhost:3000`, and the backend is
available at `http://localhost:8080`. Stop the containers with:

```bash
docker compose down
```

Database data is kept in the `orderlink-postgres` volume. Add `-v` to the
`down` command only when the local database data should also be removed.

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
| `NUXT_API_BASE` | `http://localhost:8080/api` |
| `NUXT_PUBLIC_API_BASE` | `http://localhost:8080/api` |

Docker Compose ports and database credentials can be changed in the root
`.env` file. The available values are listed in `.env.example`.

## Verification

```bash
cd backend && ./gradlew test
cd frontend && pnpm build
```
