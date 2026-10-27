# Changelog

All notable changes to keyboard-html follow the [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format. The release workflow publishes the section matching the tagged version.

## [Unreleased]

### Changed

- `manifest.json` is now parsed with Gson (2.15.0) instead of the hand-written
  mini-parser: standard JSON semantics apply (escapes such as `\t`/`\u00e9`
  decode inside the display name) and future fields become straightforward.

## [0.1.1] - 2026-10-27

### Fixed

- A keyboard that is already active picks up a layout change at once
  (chooser or re-import): the service reloads the web view instead of
  keeping the previously loaded HTML until it is closed and reopened.

## [0.1.0] - 2026-10-27

### Added

- MVP: IME service rendering the bundled HTML QWERTY keyboard via a WebView with a JS bridge (letters/space/backspace/enter), configuration screen (XML) with enable/select shortcuts, a test text field, `.zip` layout import through the SAF file picker (internal storage only), and choosing among installed layouts (persisted selection, plus a built-in default entry).
- Optional `manifest.json` in keyboard packages declares the display name (fallback: zip file name).
- Sample layout zips (`red-test.zip`, `mint-test.zip`) generated next to the debug APK for on-device smoke testing (`docs/smoke-tests.md`).
- CI on `dev` (build, lint, tests) and tag-triggered release pipeline with Gradle/SDK caches, APK artifacts and SHA256 checksums; version guards keep tag, `APP_VERSION` and Changelog in sync.
- Manual smoke-test checklist in `docs/smoke-tests.md`.

### Fixed

- Layout import keeps its own folder per picked file name; first MVP testing
  showed every import overwriting a single "import" folder.
- Keyboard display label shows only the manifest `name` (no technical id suffix).
- Backspace clears the current selection in one step instead of deleting a
  single character behind the selection.
