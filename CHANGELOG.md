# Changelog

All notable changes to keyboard-html follow the [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format. The release workflow publishes the section matching the tagged version.

## [Unreleased]

### Added

- MVP slices: IME service rendering the bundled HTML QWERTY keyboard via a WebView with a JS bridge (letters/space/backspace/enter), configuration screen (XML) with enable/select shortcuts, a test text field, `.zip` layout import through the SAF file picker (internal storage only), and choosing among installed layouts (persisted selection).
- Sample layout zips (`red-test.zip`, `mint-test.zip`) generated next to the debug APK for on-device smoke testing.
- `VersionConsistencyTest`: APP_VERSION must be semver X.Y.Z, APP_VERSION_CODE a positive integer, and CHANGELOG.md must track the project version.
- Release pipeline guards: tag must match `APP_VERSION`, CHANGELOG section must exist, APK `versionName` (debug and release) must equal the project version.
- Manual smoke-test checklist in `docs/smoke-tests.md`.

### Changed

- `APP_VERSION` bumped from `1.0` to `1.0.0` (semver, tag-compatible).

## [1.0.0] - Unreleased

### Added

- Initial app skeleton: WebView-based app shell, Gradle/Termux build setup, unit test foundation, CI and tag-triggered release pipeline.
