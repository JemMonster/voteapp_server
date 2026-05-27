# Contributing

## Workflow
1. Create feature branch
2. Implement changes with clean commit history
3. Open PR
4. Ensure tests pass

## Conventional Commits
Use:
- `feat: ...`
- `fix: ...`
- `chore: ...`
- `docs: ...`
- `test: ...`

## Code style
- Kotlin formatting enforced by project tooling (if `ktlint` is added later)
- Follow existing naming and package structure.

## PR checklist
- [ ] Unit and integration tests added/updated
- [ ] `./gradlew test` passes
- [ ] No breaking API changes without versioning note
- [ ] Env vars documented in README/DEPLOYMENT

