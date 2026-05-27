# Architecture

Clean Architecture layers (server)

## Package layout
- `com.example.voteapp.server.plugins`
  - Ktor plugin composition: routing, monitoring, auth
- `com.example.voteapp.server.votings`
  - `domain`
    - `port/VotingRepository` — interface for persistence
    - `usecase/GetVotingsUseCase` — business logic
  - `data`
    - repository implementations (currently in-memory)
  - `VotingsRoutes.kt`
    - HTTP endpoints calling use-cases
- `com.example.voteapp.server.db`
  - DB connection + schema management via Flyway

## Data flow
1. HTTP request hits a route handler in `VotingsRoutes.kt`
2. Route calls `GetVotingsUseCase`
3. Use-case calls `VotingRepository`
4. Repository returns domain model objects
5. Ktor serializes them to JSON

## DI / Composition Root
- Composition root is in `Application.module()` and per-module `*Plugin.kt`.
- `VotingsPlugin` currently wires `InMemoryVotingRepository` + `GetVotingsUseCase`.

## Authentication
- `plugins/AuthPlugin.kt`
  - Installs `Authentication` with `bearer("firebase-jwt")`
  - Verifies ID token using Firebase Admin SDK
  - On success sets `UserIdPrincipal` with Firebase uid
- Protected route usage:
  - `authenticate("firebase-jwt") { ... }`

## Error handling
- Currently auth returns `401` with `{ "error": "unauthorized" }`.
- Next step (not in this task): unify all errors via `StatusPages` + custom exceptions.

