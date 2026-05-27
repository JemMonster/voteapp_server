# TODO — VoteApp Server API expansion

## Phase 1 — Errors + status codes ✅
- [x] Update `plugins/StatusPages.kt` to return unified format: `{ "error": "...", "code": <http_code> }`
- [x] Ensure mapping for: 400/401/403/404 and internal 500 (base mapping for 400/401/404)
- [x] Add 403 mapping when access is forbidden (invite/not creator/etc.)
- [x] Update `plugins/AuthPlugin.kt` bearer challenge to return unified `{error, code}` with 401
- [x] Implement centralized `handleException()` in `VotingsRoutes.kt`

## Phase 2 — DTO / models contract ✅
- [x] Update voting creation contract: add `startTime`, `endTime`, rename `votingType` to `SINGLE_CHOICE|MULTIPLE_CHOICE`
- [x] Update vote payload contract: `optionId` or `optionIds[]` depending on voting type
- [x] Add/adjust response models: voting details, results, history, invite
- [x] Add `UpdateVotingRequest`, `UpdateVoteRequest`, `VoteResponse` models

## Phase 3 — Routes + usecases + repository ✅
- [x] Extend `GET /api/v1/votings` with query params: `status` (active/completed), `type` (SINGLE_CHOICE/MULTIPLE_CHOICE)
- [x] Add endpoints:
  - [x] `GET /api/v1/votings/{id}`
  - [x] `POST /api/v1/votings/{id}/vote` (active check + not voted yet + validate options)
  - [x] `GET /api/v1/votings/{id}/results`
  - [x] `GET /api/v1/votings/history`
  - [x] `POST /api/v1/votings/{id}/invite`
  - [x] `PUT /api/v1/votings/{id}` (update voting)
  - [x] `DELETE /api/v1/votings/{id}` (delete voting)
  - [x] `PUT /api/v1/votings/{id}/vote` (update vote)
  - [x] `DELETE /api/v1/votings/{id}/vote` (remove vote)
  - [x] `POST /api/v1/auth/register` (user registration)
  - [x] `POST /api/v1/auth/login` (user login)
- [x] Extend `VotingRepository` + implementations:
  - [x] add methods for details/history/invite
  - [x] implement DB checks for voting active, already voted, and option validation
  - [x] add `update()`, `delete()`, `updateVote()`, `removeVote()`, `hasUserVoted()`
- [x] Keep clean architecture: routes → usecase → repository → Exposed
- [x] Create `UpdateVotingUseCase`, `DeleteVotingUseCase`, `UpdateVoteUseCase`

## Phase 4 — Flyway V4 + DB schema ✅
- [x] Update `server/db/Tables.kt` with required columns (start/end) and add `Invites` table
- [x] Create `V4__improve_votes_table.sql` migration for votes table improvements
- [x] Add unique constraint on (user_id, voting_id)
- [x] Add timestamps (created_at, updated_at) to votes
- [x] Add indexes for faster lookups
- [x] Update `ExposedVotingRepository` to support new queries

## Phase 5 — Tests ⏳
- [ ] Add tests for each new endpoint via `testApplication`
- [ ] Create scenarios for success + validation errors + repeat vote + wrong email invite
- [ ] Ensure tests validate error body format `{error, code}`
- [ ] Unit tests for new use cases (6-8 use cases)
- [ ] Integration tests for all endpoints
- [ ] Achieve 60% test coverage

## Phase 6 — Documentation ✅
- [x] Update `kotlin/server/README.md`
- [x] Update `docs/api.md` to list all endpoints with request/response examples and error format
- [x] Create `DEPLOYMENT.md` with deployment instructions
- [x] Create `CHANGELOG.md` with semantic versioning
- [x] Create `SERVER_IMPLEMENTATION_REPORT.md`
- [x] Update root `README.md` with features and architecture

## Additional Improvements ✅
- [x] Create `Dockerfile` for containerization
- [x] Create `docker-compose.yml` for local development
- [x] Set up GitHub Actions CI/CD workflows
- [x] Add authentication and authorization (Firebase JWT)
- [x] Implement owner-only authorization for voting operations
- [x] Add comprehensive input validation

## Future Enhancements 🚧
- [ ] Add pagination to voting list endpoints
- [ ] Implement rate limiting
- [ ] Add OpenAPI/Swagger documentation
- [ ] Add caching layer (Redis)
- [ ] Add notification system
- [ ] Add raffle and petition voting types
- [ ] Implement vote analytics
- [ ] Add export results feature (PDF/CSV)

---

## Completed Status Summary

✅ **Phases 1-4, 6:** Fully completed
⏳ **Phase 5:** Tests pending implementation
🚧 **Future Enhancements:** Planned for next iterations

**Overall Progress:** ~85% complete (tests remaining)


