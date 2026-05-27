# TODO — VoteApp Server API expansion

## Phase 1 — Errors + status codes
- [ ] Update `plugins/StatusPages.kt` to return unified format: `{ "error": "...", "code": <http_code> }`
- [ ] Ensure mapping for: 400/401/403/404 and internal 500
- [ ] Update `plugins/AuthPlugin.kt` bearer challenge to return unified `{error, code}` with 401

## Phase 2 — DTO / models contract
- [ ] Update voting creation contract: add `startTime`, `endTime`, rename `votingType` to `SINGLE_CHOICE|MULTIPLE_CHOICE`
- [ ] Update vote payload contract: `optionId` or `optionIds[]` depending on voting type
- [ ] Add/adjust response models: voting details, results, history, invite

## Phase 3 — Routes + usecases + repository
- [ ] Extend `GET /api/v1/votings` with query params: `status` (active/completed), `type` (SINGLE_CHOICE/MULTIPLE_CHOICE)
- [ ] Add endpoints:
  - [ ] `GET /api/v1/votings/{id}`
  - [ ] `POST /api/v1/votings/{id}/vote` (active check + not voted yet + validate options)
  - [ ] `GET /api/v1/votings/{id}/results`
  - [ ] `GET /api/v1/votings/history`
  - [ ] `POST /api/v1/votings/{id}/invite`
- [ ] Extend `VotingRepository` + implementations:
  - [ ] add methods for details/history/invite
  - [ ] implement DB checks for voting active, already voted, and option validation
- [ ] Keep clean architecture: routes → usecase → repository → Exposed

## Phase 4 — Flyway V4 + DB schema
- [ ] Update `server/db/Tables.kt` with required columns (start/end) and add `Invites` table
- [ ] Create `V4__*.sql` migration for invites + any schema changes
- [ ] Update `ExposedVotingRepository` to support new queries

## Phase 5 — Tests
- [ ] Add tests for each new endpoint via `testApplication`
- [ ] Create scenarios for success + validation errors + repeat vote + wrong email invite
- [ ] Ensure tests validate error body format `{error, code}`

## Phase 6 — Documentation
- [ ] Update `kotlin/server/README.md`
- [ ] Update `docs/api.md` to list all endpoints with request/response examples and error format


