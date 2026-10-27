# AGENTS.md

Android app template designed for building **inside Termux** (and also works on desktop Linux). Single module (`app`), plain Java, no Kotlin.

## Build

- `./gradlew` on first run auto-installs the Android SDK (via `com.quittle.setup-android-sdk` plugin) into `build/android-sdk-root` and writes `local.properties` (`sdk.dir`). Only JDK 17 is required beforehand; on Termux also install `aapt2` (`pkg install openjdk-17 aapt2`) — it's wired through `android.aapt2FromMavenOverride` in `gradle.properties` (commented/uncomment there).
- Build APK: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk` (no `installDebug` target device path in Termux; install by copying the APK to `/sdcard/Download`).
- On low-RAM devices uncomment `org.gradle.jvmargs=-Xmx512m` in `gradle.properties` or builds can kill the phone.

## Configuration quirks

- **No hardcoded values in build files**: app ID, versions, SDK levels are read from `gradle.properties` (`APP_PACKAGE`, `APP_VERSION`, `APP_VERSION_CODE`, `APP_MIN_SDK_VERSION`, `APP_TARGET_SDK_VERSION`, `ANDROID_COMPILE_SDK_VERSION`, `ANDROID_BUILD_TOOLS_VERSION`, `ANDROID_GRADLE_PLUGIN_VERSION`). Root `build.gradle` throws if any is missing — adding app config means editing `gradle.properties`, not `app/build.gradle`.
- `APP_PACKAGE` must match the Java package under `app/src/main/java/` (`com.example.myapp` currently) — it's used as both `namespace` and `applicationId`.
- Java 17 source/target compatibility is fixed in `app/build.gradle`.
- `local.properties` is generated/gitignored-adjacent; don't commit or hand-edit `sdk.dir`.
- License acceptance for the auto-installed SDK lives in `app/licenses/android-sdk-license`; `LICENSES_DIR` env var can override the licenses directory.

## Project purpose / intended app (user requirements)

This template is the base for an experimental on-screen keyboard app:

- Keyboard UI is **HTML rendered in a WebView**; the web layer talks to the Android keyboard service via a **JS↔Android API** (JavascriptInterface or equivalent).
- Keyboard layouts are user-importable **.zip packages**, extracted into the app's **internal storage** — never use broad storage permissions; use the system file manager via intent (SAF) for picking the zip.
- Default bundled keyboards (QWERTY etc.) ship as HTML assets inside the APK.
- The app must **work fully offline** — no network dependencies at runtime.

## Workflow rules (must follow)

- **TDD**: write the failing test first, then implement, then refactor. Every new feature or fix starts with a test. Run tests with `./gradlew test` (single test: `./gradlew :app:testDebugUnitTest --tests '<FullyQualifiedClassName>'`).

- **Git flow + Conventional Commits**: branch off `develop` (`feat/...`, `fix/...`, `release/...`, `hotfix/...`, ...). Commit messages follow Conventional Commits (`feat:`, `fix:`, `chore:`, `refactor:`, `docs:`, `test:`, ...). Merge through a release branch into `main`; stable versions are marked by creating and **pushing a git tag on `main`**:

  ```bash
  git checkout main && git merge --no-ff release/x.y.z
  git tag -a vX.Y.Z -m "vX.Y.Z" && git push origin main --tags
  ```

- All docs, comments and commit messages in **English**; no Portuguese content in the repo.

## Docs

`README.md` covers the Termux setup walkthrough (it overlaps with the above build notes).
