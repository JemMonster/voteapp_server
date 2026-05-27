# VoteApp Server API

RESTful API documentation for the VoteApp voting platform. All endpoints require a valid Firebase JWT in the `Authorization` header unless stated otherwise.

## 🇷🇺 Русская версия

### Базовый путь
Все эндпоинты доступны по префиксу: `/api/v1`  
Base URL: `http://localhost:8080/api/v1`

### Требования к авторизации
Для защищённых эндпоинтов требуется заголовок:
Authorization: Bearer <firebase_id_token>

Токен верифицируется сервером через Firebase Admin SDK. Если токен отсутствует или невалиден, сервер возвращает `401 Unauthorized` с форматом: `{"message": "unauthorized"}`.

### Формат ошибок (бизнес-логика)
Все бизнес-ошибки возвращаются в едином формате (согласно `StatusPages.kt`):
```json
{ "message": "Описание ошибки" }


400
Валидация входных данных
{"message": "Validation failed: title cannot be blank"}
ValidationException
400
Голосование уже закрыто
{"message": "Voting already closed"}
VotingAlreadyClosedException
401
Отсутствует или невалиден токен
{"message": "unauthorized"}
AuthPlugin.kt challenge
404
Голосование не найдено
{"message": "Voting not found"}
VotingNotFoundException
409
Пользователь уже проголосовал
{"message": "Already voted in this voting"}
AlreadyVotedException
500
Внутренняя ошибка сервера
{"message": "Internal server error"}
StatusPages.kt

Справочник эндпоинтов
GET /api/v1/votings
Возвращает список всех активных голосований.
Требования: Аутентификация
Ответ 200 OK:
{
  "votings": [
    {
      "id": 123,
      "title": "Выбор темы курсовой",
      "type": "SINGLE",
      "status": "ACTIVE",
      "endsAt": "2026-06-01T23:59:59Z",
      "totalVotes": 42,
      "hasVoted": false
    }
  ]
}

Пример curl:
curl -s -X GET \
  "http://localhost:8080/api/v1/votings" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN"

POST /api/v1/votings
Создаёт новое голосование.
Требования: Аутентификация
Тело запроса (application/json):
{
  "title": "Выбор даты выезда",
  "description": "Опциональное описание",
  "imageUrl": null,
  "type": "MULTIPLE",
  "durationDays": 15,
  "options": ["10 июня", "15 июня", "20 июня"]
}

Валидации:
title — обязателен, не пустая строка
durationDays — диапазон 1..360
Для SINGLE и MULTIPLE — минимум 2 опции в массиве options
Ответ 201 Created:
{
  "id": 123,
  "title": "Выбор даты выезда",
  "description": "Опциональное описание",
  "type": "MULTIPLE",
  "status": "ACTIVE",
  "imageUrl": null,
  "endsAt": "2026-06-05T23:59:59Z",
  "totalVotes": 0,
  "hasVoted": false
}

Заголовок ответа: Location: /api/v1/votings/123
Пример curl:
curl -s -X POST \
  "http://localhost:8080/api/v1/votings" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Выбор даты выезда",
    "description": null,
    "imageUrl": null,
    "type": "MULTIPLE",
    "durationDays": 15,
    "options": ["10 июня", "15 июня", "20 июня"]
  }'

POST /api/v1/votings/{id}/vote
Регистрирует голос пользователя.
Требования: Аутентификация
Путь: /api/v1/votings/{id}/vote
Тело запроса:
{
  "selectedOptionIds": [1],
  "isParticipating": true
}

Логика валидации по типу голосования:
VotingType   Требование к selectedOptionIds   Требование к isParticipating
SINGLE   Ровно 1 элемент   Не проверяется
MULTIPLE   От 1 до N элементов   Не проверяется
PETITION   Игнорируется   Должен быть true
GIVEAWAY   Ровно 1 элемент   Должен быть true

Ответ 200 OK:
{
  "message": "Vote recorded successfully"
}

Пример curl:
curl -s -X POST \
  "http://localhost:8080/api/v1/votings/123/vote" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "selectedOptionIds": [1],
    "isParticipating": true
  }'

GET /api/v1/votings/{id}/results
Возвращает текущие результаты голосования.
Требования: Аутентификация
Ответ 200 OK:
{
  "votingId": 123,
  "status": "ACTIVE",
  "type": "MULTIPLE",
  "totalParticipants": 42,
  "optionsResults": [
    { "optionId": 1, "text": "10 июня", "percent": 42.8, "votesCount": 18 },
    { "optionId": 2, "text": "15 июня", "percent": 57.2, "votesCount": 24 }
  ],
  "signaturesCount": null,
  "winnerInfo": { "winnerUserId": "user-uuid-123" }
}

Пример curl:
curl -s -X GET \
  "http://localhost:8080/api/v1/votings/123/results" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN"

GET /api/v1/auth/health
Проверка работоспособности модуля аутентификации.
Требования: Не требуется
Ответ 200 OK:
{ "status": "ok", "module": "auth" }

GET /api/v1/profile/health
Проверка работоспособности модуля профилей.
Требования: Не требуется
Ответ 200 OK:
{ "status": "ok", "module": "profile" }

Перечисления (Enums)
Enum   Значения
VotingType   SINGLE, MULTIPLE, PETITION, GIVEAWAY
VotingStatus   ACTIVE, CLOSED

Модели данных
NewVoting (запрос на создание)
Поле   Тип   Обязательное   Описание

title
String
✅
Название голосования
description
String?
❌
Описание (опционально)
imageUrl
String?
❌
URL изображения
type
VotingType
✅
Тип голосования
durationDays
Int
✅
Длительность в днях (1..360)
options
List<String>?
❌
Список вариантов (для SINGLE/MULTIPLE)





