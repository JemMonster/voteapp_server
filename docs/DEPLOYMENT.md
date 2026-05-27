# Deployment

## PostgreSQL
Use Neon / Supabase / any Postgres.

Set env vars:
- `DATABASE_URL` (e.g. `jdbc:postgresql://<host>:5432/<db>`)
- `DATABASE_USER`
- `DATABASE_PASSWORD`

Migrations:
- Put SQL migrations in `kotlin/server/src/main/resources/db/migration`
- Enable Flyway at startup:
  - `FLYWAY_MIGRATE=true`

## Firebase Admin SDK
Authentication is done via Firebase ID token verification on the server.

Set:
- `GOOGLE_APPLICATION_CREDENTIALS` — path to the **server service account** JSON.

> Note: `context/google-services.json` is for mobile client and is not sufficient for Firebase Admin verification.
> Use Firebase Console -> Project Settings -> Service Accounts -> Generate new private key.

## Deploy examples
### Render
- Environment variables: `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `GOOGLE_APPLICATION_CREDENTIALS`, `FLYWAY_MIGRATE`, `PORT`
- Start command:
  - `./gradlew run`
  - or use a fat jar: `java -jar build/libs/*.jar`

### Fly.io
- Configure secrets for DB + Firebase.
- Ensure file system access for the service account JSON path (or mount it during build).

## Healthcheck
Recommend an HTTP health endpoint for platform:
- `GET /` or `GET /api/v1/auth/health`

## Notes
- The API requires valid `Authorization: Bearer <idToken>` for protected routes.

