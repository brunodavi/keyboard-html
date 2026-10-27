# Build environment

Details for building `keyboard-html` locally (Termux first, desktop Linux also works).

## Requirements

- JDK 17 (`pkg install openjdk-17` on Termux).
- On Termux only: `aapt2` from the repos (`pkg install aapt2`), wired through `android.aapt2FromMavenOverride` in `gradle.properties` (comment/uncomment there).
- Everything else (Android SDK, build tools, platforms) is installed automatically on first Gradle run.

## First run

`./gradlew` auto-installs the Android SDK (via the `com.quittle.setup-android-sdk` plugin) into `build/android-sdk-root` and writes `local.properties` (`sdk.dir`). No manual SDK setup needed.

## Tasks

- Full test run: `./gradlew test`.
- Single test: `./gradlew :app:testDebugUnitTest --tests '<FullyQualifiedClassName>'`.
- Debug APK: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk` (auto-signed with the debug key, installable directly after enabling unknown sources).
- Lint: `./gradlew lint` → reports under `app/build/reports/`.

There is no `installDebug` target usable in Termux; install by copying the APK to `/sdcard/Download` and opening it from a file manager.

## Configuration quirks

- **No hardcoded values in build files**: app ID, display name, versions, SDK levels are read from `gradle.properties` (`APP_PACKAGE`, `APP_NAME`, `APP_VERSION`, `APP_VERSION_CODE`, `APP_MIN_SDK_VERSION`, `APP_TARGET_SDK_VERSION`, `ANDROID_COMPILE_SDK_VERSION`, `ANDROID_BUILD_TOOLS_VERSION`, `ANDROID_GRADLE_PLUGIN_VERSION`). Root `build.gradle` throws if any is missing — adding app config means editing `gradle.properties`, not `app/build.gradle`.
- `APP_PACKAGE` must match the Java package under `app/src/main/java/` (`com.keyboardhtml` currently) — it's used as both `namespace` and `applicationId`.
- `APP_NAME` sets the app display name (launcher label) and is injected as a manifest placeholder — no hardcoded label in the manifest.
- Java 17 source/target compatibility is fixed in `app/build.gradle`.
- `local.properties` is generated/gitignored; don't commit or hand-edit `sdk.dir`. If you copy the project folder, regenerate it (a stale absolute path breaks the SDK resolution and may also poison Gradle's compiled-script caches — wipe `.gradle` and `app/build` if that happens).
- License acceptance for the auto-installed SDK lives in `app/licenses/android-sdk-license`; `LICENSES_DIR` env var can override the licenses directory.

## Memory

Gradle on mobile devices can be memory-intensive. To limit it on low-RAM devices, uncomment in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx512m
```
