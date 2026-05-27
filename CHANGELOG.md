## [Unreleased] - 2024-XX-XX

### Added
- Full CRUD operations for votings (POST, GET, PUT, DELETE)
- Vote management (POST, PUT, DELETE for votes) - cast, update, and remove votes
- User authentication with Firebase JWT
- User registration (`POST /api/v1/auth/register`) and login (`POST /api/v1/auth/login`) endpoints
- Comprehensive error handling with standardized error format (`error`, `message`)
- Input validation for all endpoints (title length, email format, voting dates, etc.)
- Proper HTTP status codes (201, 400, 401, 403, 404, 409)
- Docker support with Dockerfile and docker-compose.yml
- Database schema improvements with Flyway migrations (V4__improve_votes_table.sql)
- API documentation in docs/api.md
- Deployment guide (DEPLOYMENT.md)
- Authorization/Ownership checks for voting updates and deletions
- Vote modification and removal capabilities

### Changed
- Enhanced `ExposedVotingRepository` with update, delete, and vote management methods
- Updated `VotingsRoutes` with proper HTTP methods (PUT, DELETE) and centralized error handling
- Improved `AuthPlugin` with registration and login support via `configureAuth()`
- Extended `Voting` domain model with `creatorId` and `createdAt` fields
- Updated `AuthRoutes` to handle registration and login with proper error responses
- Modified `Application.module()` to initialize auth use-cases

### Fixed
- Server shutdown hook now correctly calls `DatabaseFactory.close()`
- Proper JWT authentication middleware for protected routes
- Consistent error responses across all endpoints
- Database schema now includes timestamps for votes table

## 2026-05-21
- [server] Введены понятные слои/пакеты для сервера: `config/`, `auth/`, `profile/`, `votings/`.
- [server] Роут `/api/v1/votings` вынесен в модуль `votings`.
- [server] DB connect теперь берёт `DATABASE_URL` из `server/config/AppConfig.kt`.

## 2026-05-21 (Clean Architecture)
- [server] В `votings` выделены Clean Architecture слои: `domain` port `VotingRepository` и use case `GetVotingsUseCase`.
- [server] Роуты `VotingsRoutes` вызывают use case, а in-memory данные вынесены в `data` реализацию `InMemoryVotingRepository`.
- [server] Dependency (repo/use case) собрана в `VotingsPlugin` как composition root для модуля `votings`.



