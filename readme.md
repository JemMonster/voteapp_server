# voteapp_server

Production-ready backend for the Vote App.

## Stack
- Kotlin 1.9+
- Ktor 2.3+
- Exposed 0.47+ (project currently uses Exposed + Flyway migrations)
- PostgreSQL
- HikariCP
- Firebase Admin SDK (verify Firebase ID tokens)

## Modules / Architecture (Clean)
- `com.example.voteapp.server.plugins` — Ktor plugins (routing, monitoring, auth)
- `com.example.voteapp.server.votings` — voting use-cases (ports/domain/data)
- `com.example.voteapp.server.db` — DB wiring (Flyway + Exposed)
- `com.example.voteapp.server.auth/profile` — API surface for auth/profile (currently limited)

See `docs/architecture.md`.

## Requirements
- Java 17+
- PostgreSQL (Neon/Supabase/any Postgres)
- Firebase service account JSON for Admin SDK

## Environment variables
Copy example:
- `cp .env.example .env` (or create env vars in your platform)

Required for PostgreSQL:
- `DATABASE_URL` (JDBC URL)
- `DATABASE_USER`
- `DATABASE_PASSWORD`

Optional for Flyway:
- `FLYWAY_MIGRATE` (set to `true` to run migrations at startup)

Required for Firebase Admin SDK:
- `GOOGLE_APPLICATION_CREDENTIALS` — absolute path to **service account JSON**

Networking:
- `PORT` (default `8080`)

## Run locally
From repo root:
```bash
cd kotlin/server
./gradlew run
```

The server listens on `http://localhost:8080`.

## Health
- `GET /` — server status
- `GET /api/v1/auth/health` — auth module health (no auth required)
- `GET /api/health` (if present via `HealthRoutes`)

## API (quick)
- `GET /api/v1/votings` — requires `Authorization: Bearer <firebase-id-token>`

Examples: `docs/api.md`.

