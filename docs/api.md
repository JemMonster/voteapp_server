# API

Base URL: `http://localhost:8080/api/v1`

## Auth
### Health
`GET /auth/health`
- Response: `200 {"status":"ok","module":"auth"}`

## Votings
### List votings
`GET /votings`
- Requires: `Authorization: Bearer <firebase-id-token>`

Responses:
- `200` — list of votings (domain model JSON)
- `401` — missing/invalid token

Example curl:
```bash
curl -s -X GET \
  "http://localhost:8080/api/v1/votings" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN"
```

### 401 behavior
If token is missing or invalid, server returns:
- HTTP `401`
- body: `{ "error": "unauthorized" }`

