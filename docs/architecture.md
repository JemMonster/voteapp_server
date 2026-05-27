# Architecture

Clean Architecture layers (server)

## 🇷🇺 Русская версия

### Архитектура (Clean Architecture)


Сервер разделён на слои:

- **presentation** — HTTP-эндпоинты (Ktor routes)
  - `com.example.voteapp.server.votings.VotingsRoutes.kt`
  - `com.example.voteapp.server.auth.AuthRoutes.kt`
  - `com.example.voteapp.server.profile.ProfileRoutes.kt`

- **domain** — бизнес-логика и контракты (use-cases + порты)
  - `com.example.voteapp.server.votings.domain.usecase.*`
  - `com.example.voteapp.server.votings.domain.port.VotingRepository`

- **data** — реализации портов (доступ к данным)
  - `ExposedVotingRepository` — PostgreSQL (Exposed) + миграции Flyway
  - `InMemoryVotingRepository` — существует, но предназначена для локальной разработки/временного режима (см. TODO в коде) и **не используется** как основной data-layer в `VotingsPlugin`.

- **composition root / wiring** — сборка зависимостей
  - `com.example.voteapp.server.Application` и `*Plugin.kt` (в частности `VotingsPlugin.kt`)

## Схема пакетов (package layout)

- `com.example.voteapp.server.plugins`
  - Ktor plugins (routing, monitoring, auth)
- `com.example.voteapp.server.votings`
  - `domain`
    - `port/VotingRepository` — интерфейс для доступа к данным
    - `usecase/*` — use-cases (business logic):
      - `GetVotingsUseCase`
      - `CreateVotingUseCase`
      - `VoteUseCase`
      - `GetResultsUseCase`
  - `data`
    - реализации репозитория: `ExposedVotingRepository` (основная) и `InMemoryVotingRepository` (для локального режима)
  - `VotingsRoutes.kt`
    - HTTP endpoints, вызывающие use-cases
- `com.example.voteapp.server.db`
  - DB connection + schema management via Flyway

## Поток данных (data flow)

1. HTTP запрос попадает в handler в `VotingsRoutes.kt`.
2. Маршрут вызывает соответствующий use-case.
3. Use-case обращается к `VotingRepository` (порт).
4. Репозиторий возвращает доменные модели/результаты.
5. Ktor сериализует ответ в JSON.

## DI / Composition Root

- Composition root находится в `Application.module()` и далее включается через `*Plugin.kt`.
- `VotingsPlugin` подключает **`ExposedVotingRepository`** и создаёт необходимые use-cases для модуля `votings`.

## Аутентификация

- `plugins/AuthPlugin.kt`
  - Устанавливает `Authentication` с `bearer("firebase-jwt")`
  - Проверяет Firebase ID token через Firebase Admin SDK
  - При успехе формирует `UserIdPrincipal` (Firebase uid)

Использование защищённых роутов:
- `authenticate("firebase-jwt") { ... }`

## Обработка ошибок

- Единый формат бизнес-ошибок задаётся `StatusPages`:
  - `ValidationException` → `400` с JSON `{ "message": "..." }`
  - `VotingNotFoundException` → `404` с JSON `{ "message": "..." }`
  - `VotingAlreadyClosedException` → `400`
  - `AlreadyVotedException` → `409`
- Неклассифицированные исключения → `500` с `{ "message": "Internal server error" }`.

> Важно: формат ответа для auth-ошибок задаёт `Authentication` challenge, а не `StatusPages`.

## 🇬🇧 English Version

## Architecture (Clean Architecture)

The server is split into layers:

- **presentation** — HTTP endpoints (Ktor routes)
  - `com.example.voteapp.server.votings.VotingsRoutes.kt`
  - `com.example.voteapp.server.auth.AuthRoutes.kt`
  - `com.example.voteapp.server.profile.ProfileRoutes.kt`

- **domain** — business logic and contracts (use-cases + ports)
  - `com.example.voteapp.server.votings.domain.usecase.*`
  - `com.example.voteapp.server.votings.domain.port.VotingRepository`

- **data** — implementations of ports (data access)
  - `ExposedVotingRepository` — PostgreSQL via Exposed + Flyway migrations
  - `InMemoryVotingRepository` — exists for local development / temporary mode (see TODO in the code) and **is not** the primary data-layer in `VotingsPlugin`.

- **composition root / wiring** — dependency wiring
  - `com.example.voteapp.server.Application` and `*Plugin.kt` (including `VotingsPlugin.kt`)

## Package layout

- `com.example.voteapp.server.plugins`
  - Ktor plugins (routing, monitoring, auth)
- `com.example.voteapp.server.votings`
  - `domain`
    - `port/VotingRepository` — persistence port
    - `usecase/*` — business use-cases:
      - `GetVotingsUseCase`
      - `CreateVotingUseCase`
      - `VoteUseCase`
      - `GetResultsUseCase`
  - `data`
    - repository implementations: `ExposedVotingRepository` (main) and `InMemoryVotingRepository` (local mode)
  - `VotingsRoutes.kt`
    - HTTP endpoints calling use-cases
- `com.example.voteapp.server.db`
  - DB connection + schema management via Flyway

## Data flow

1. An HTTP request is handled by a route in `VotingsRoutes.kt`.
2. The route calls the corresponding use-case.
3. The use-case uses `VotingRepository` (port).
4. The repository returns domain models/results.
5. Ktor serializes the response as JSON.

## DI / Composition Root

- Composition root is in `Application.module()` and per-module `*Plugin.kt`.
- `VotingsPlugin` wires **`ExposedVotingRepository`** and creates use-cases for the `votings` module.

## Authentication

- `plugins/AuthPlugin.kt`
  - Installs `Authentication` with `bearer("firebase-jwt")`
  - Verifies Firebase ID token using Firebase Admin SDK
  - On success sets `UserIdPrincipal` (Firebase uid)

Protected route usage:
- `authenticate("firebase-jwt") { ... }`

## Error handling

- Business errors are formatted via `StatusPages`:
  - `ValidationException` → `400` with JSON `{ "message": "..." }`
  - `VotingNotFoundException` → `404` with JSON `{ "message": "..." }`
  - `VotingAlreadyClosedException` → `400`
  - `AlreadyVotedException` → `409`
- Unhandled exceptions → `500` with `{ "message": "Internal server error" }`.

> Note: authentication failures format is controlled by the `Authentication` challenge, not `StatusPages`.


