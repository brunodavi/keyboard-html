# Changelog

All notable changes to keyboard-html follow the [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format. The release workflow publishes the section matching the tagged version.

## [Unreleased]

### Added

- Built-in keyboard replaced with a complete dark Gboard-style QWERTY:
  three sheets (letters, symbols 1/2, symbols 2/2 incl. currency, math and
  inverted marks), one-shot shift, page toggle (`2/2`/`1/2`), and the
  `!#1`/`ABC` sheet switch; special keys are UI-only and never leak text
  through the bridge (spec-tested in `DefaultKeyboardAssetTest`).
- Letters row is a shared 20-column CSS grid (on-device tested variant):
  uniform key widths, shift/backspace span three columns each.

### Fixed

- Crash when reselecting Keyboard HTML after switching to another IME:
  the layout reload used startService, which starts/stickily restarts the
  InputMethodService outside the input-method lifecycle; replaced with an
  in-app broadcast received by the service.

### Changed

- Release builds now run R8 (code + resource shrinking): the release APK
  compresses the unneeded Gson classes away instead of dexing the whole
  library; explicit ProGuard keeps protect the `@JavascriptInterface`
  bridge from being stripped (spec-tested in `ReleaseBuildShrinksCodeTest`).
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
