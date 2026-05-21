## 2026-05-21
- [server] Введены понятные слои/пакеты для сервера: `config/`, `auth/`, `profile/`, `votings/`.
- [server] Роут `/api/v1/votings` вынесен в модуль `votings`.
- [server] DB connect теперь берёт `DATABASE_URL` из `server/config/AppConfig.kt`.

## 2026-05-21 (Clean Architecture)
- [server] В `votings` выделены Clean Architecture слои: `domain` port `VotingRepository` и use case `GetVotingsUseCase`.
- [server] Роуты `VotingsRoutes` вызывают use case, а in-memory данные вынесены в `data` реализацию `InMemoryVotingRepository`.
- [server] Dependency (repo/use case) собрана в `VotingsPlugin` как composition root для модуля `votings`.



