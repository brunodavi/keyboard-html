# AGENTS.md — Work Guide

`keyboard-html` — Android on-screen keyboard app, HTML keyboard layouts rendered in a WebView, user-importable `.zip` packages, fully offline.

**Rule: this file only points to the work. All technical depth (how it's built, why, how it's released) lives in `docs/` — add any new technical documentation there, not here.**

## Read before working

- `docs/build.md` — environment, build tasks, `gradle.properties` configuration quirks (Termux specifics)
- `docs/architecture.md` — project requirements and design decisions (WebView + JS↔Android API, .zip layout packages, offline)
- `docs/workflow.md` — mandatory workflow: TDD, commits directly on `dev` (tags mark production/“prd”), Conventional Commits, English-only repo
- `docs/deployment.md` — the standard release procedure (bump version → Changelog section → tag → tag-triggered GitHub release); **always follow it, never improvise deploys**

## Quick rules

- TDD: failing test first, implement, refactor. `./gradlew test`
- Commits: directly on `dev`, Conventional Commits (`docs:`, `feat:`, `fix:`, ...); docs/comments/commits in English only
- Production = git tags (`vX.Y.Z` matching `APP_VERSION`), never branches

## Docs

`README.md` stays user/problem-facing; contributors get details from the `docs/` files above.
