## 2026-05-21
- [server] Введены понятные слои/пакеты для сервера: `config/`, `auth/`, `profile/`, `votings/`.
- [server] Роут `/api/v1/votings` вынесен в модуль `votings`.
- [server] DB connect теперь берёт `DATABASE_URL` из `server/config/AppConfig.kt`.

