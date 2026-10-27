# Workflow (rules every contributor/agent must follow)

## TDD

Write the failing test first, then implement, then refactor. Every product feature or fix starts with a test.

- Full run: `./gradlew test`
- Single test: `./gradlew :app:testDebugUnitTest --tests '<FullyQualifiedClassName>'`

## Git: single branch, tags as production ("prd")

- **Everything is committed directly on `dev`** — the only branch there is (default on GitHub). Never create feature/work branches (`feat/...`, `fix/...`, `main`, `develop`, `release/`, `hotfix/`, ...).
- Commit messages follow **Conventional Commits** (`feat:`, `fix:`, `chore:`, `refactor:`, `docs:`, `ci:`, `test:`, ...).
- Production is marked by **tags, not branches**: whenever `dev` reaches a stable state, create and push an annotated tag (version must match `APP_VERSION` in `gradle.properties`). The tag also triggers the release pipeline (see `docs/deployment.md`):

  ```bash
  git tag -a vX.Y.Z -m "vX.Y.Z" && git push origin dev --tags
  ```

## Language

All docs, comments and commit messages in **English**; no Portuguese content in the repo.

## Documentation split

`AGENTS.md` only points to the work — it never holds technical depth. All technical content (build details, architecture decisions, deployment pipeline) lives in `docs/`.

## Deployment standard (for agents)

Never improvise a release procedure: follow the standard in `docs/deployment.md` (bump `APP_VERSION` + Changelog entry, tag `v*.*.*`, let the tag-triggered workflow build/sign/checksum the APKs, publish the release notes from the matching Changelog section).

## Standard AI review prompts

After opening a PR or finishing a work session, run a structured audit instead of trusting the author's description. Use prompts in this spirit (adapt to the change):

- Per-PR: *"Audit the opened PR's code in depth — don't trust the author's description. Verify it makes sense, check for regressions or quality drops, confirm test coverage is adequate, then state a recommendation."*
- After a long session/several merged commits: *"Audit the changed code: look for dead code, unnecessary duplication, hardcoded magic values that should be constants or documented, missing test coverage; check against clean-code principles and verify documentation was updated."*

Fix whatever it flags, prioritized by current needs. The human stays in control and judges what is actually good or bad — agents propose, humans decide.
