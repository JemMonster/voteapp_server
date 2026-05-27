# Vote App — Server (Ktor + PostgreSQL + Firebase Auth)

Production-ready backend for the Vote App.

<p align="center">
  <img src="https://img.shields.io/badge/Server-Ktor-2E7D32?style=for-the-badge&logo=ktor&logoColor=white"/>
  <img src="https://img.shields.io/badge/DB-PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Auth-Firebase%20Admin%20SDK-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
  <img src="https://img.shields.io/badge/Migrations-Flyway-6DB33F?style=for-the-badge"/>
</p>

---

## О проекте
Серверная часть мобильного приложения для голосований.

- REST API
- Clean Architecture (presentation → domain → data)
- Аутентификация через **Firebase ID Token** (Firebase Admin SDK)
- PostgreSQL + Exposed + Flyway migrations

---

## Функциональность (в текущей курсовой версии)
- `GET /api/v1/votings` — получить список голосований (требует Authorization)

> Примечание: серверная часть уже содержит структуры для создания голосований, голосования и результатов (use-cases + модели), и их развитие описано в TODO/roadmap.

---

## Архитектура

```
presentation/
  ktor routes (HTTP endpoints)

domain/
  use-cases + ports (interfaces)

data/
  repository implementations (Exposed / in-memory)

plugins/
  ktor plugins: routing, auth, status pages
```

Текстовая схема package layout:
- `com.example.voteapp.server.plugins`
  - Ktor plugins (routing, monitoring, auth)
- `com.example.voteapp.server.votings`
  - `domain` — use-cases и порт `VotingRepository`
  - `data` — `ExposedVotingRepository` (PostgreSQL) + `InMemoryVotingRepository` (локальный режим)
  - `VotingsRoutes.kt` — HTTP endpoints
- `com.example.voteapp.server.db`
  - DB wiring + схема через Flyway

Схема data flow:
1) HTTP запрос → route
2) route вызывает use-case
3) use-case вызывает `VotingRepository`
4) repository обращается к данным (Exposed)
5) Ktor сериализует ответ в JSON

---

## Стек технологий
| Категория | Библиотека |
|---|---|
| Язык | Kotlin 1.9+ |
| Framework | Ktor |
| Сериализация | kotlinx.serialization (Ktor ContentNegotiation) |
| HTTP auth | Firebase Admin SDK + Ktor bearer auth |
| DB | PostgreSQL |
| ORM | Exposed |
| Connection pool | HikariCP |
| Миграции | Flyway |
| Тесты | JUnit + Ktor server tests |
|

---

## Запуск

### 1) Требования
- Java 17+
- PostgreSQL
- Firebase service account JSON

### 2) Environment variables

Required for PostgreSQL:
- `DATABASE_URL` (JDBC URL)
- `DATABASE_USER`
- `DATABASE_PASSWORD`

Optional для Flyway:
- `FLYWAY_MIGRATE` (true — прогонять миграции при старте)

Required for Firebase Admin SDK:
- `GOOGLE_APPLICATION_CREDENTIALS` — абсолютный путь к service account JSON

Networking:
- `PORT` (default 8080)

---

### 3) Запуск локально
Из директории `kotlin/server`:

```bash
./gradlew run
```

Сервер слушает на:
- `http://localhost:8080`

---

## Health
- `GET /` — server status
- `GET /api/v1/auth/health` — auth module health (без авторизации)

---

## API
Полная документация endpoints — в `docs/api.md`.

Коротко:
- `GET /api/v1/votings` — требует `Authorization: Bearer <firebase-id-token>`

---

## Roadmap
- TODO по развитию функциональности (create voting, vote, results, invites, profile, notifications): см. `../TODO.md`.

