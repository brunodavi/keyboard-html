# keyboard-html

[![CI](https://github.com/brunodavi/keyboard-html/actions/workflows/ci.yml/badge.svg?branch=dev)](https://github.com/brunodavi/keyboard-html/actions/workflows/ci.yml)
[![Release](https://github.com/brunodavi/keyboard-html/actions/workflows/release.yml/badge.svg)](https://github.com/brunodavi/keyboard-html/actions/workflows/release.yml)

**Fully customized Android keyboards, without recompiling an APK.**

Want a different keyboard layout, theme, or behavior on Android? Today that means editing native code and rebuilding the app every time. keyboard-html turns a keyboard into something you can copy, tweak, and share as a plain `.zip` of web files — edit, import, use. No rebuild, no waiting, no developer license needed.

## What problem does this solve?

- **Every tiny tweak currently requires a full APK rebuild** — here, a keyboard is just files you edit.
- **Customization is locked to developers** — here, anyone comfortable with HTML/CSS can author or restyle a keyboard.
- **Keyboard apps often drag along more permissions than they need** — custom layout packages import through the system file picker into the app's internal storage, nothing asks for your files.

## Install

Grab the latest APK from the [Releases page](https://github.com/brunodavi/keyboard-html/releases/latest) (verify with the `SHA256SUMS.txt` published in the release notes), then install it like any APK (allow unknown sources).

Prefer building it yourself in Termux? One command each:

```bash
pkg install git openjdk-17 aapt2
git clone https://github.com/brunodavi/keyboard-html.git && cd keyboard-html && ./gradlew assembleDebug
```

The SDK downloads automatically on first run. The APK appears at `app/build/outputs/apk/debug/app-debug.apk`; details in [`docs/build.md`](docs/build.md).

## Status

Work in progress — the WebView keyboard shell, package import, and default built-in layouts are landing incrementally. Track what's next in the [issues](https://github.com/brunodavi/keyboard-html/issues) and what shipped in the [CHANGELOG](CHANGELOG.md).

## Documentation (for contributors)

Feature details, architecture decisions, and house rules live under [`docs/`](docs/):

- [`docs/build.md`](docs/build.md) — build environment, configuration quirks, Termux specifics
- [`docs/architecture.md`](docs/architecture.md) — design decisions and app requirements
- [`docs/workflow.md`](docs/workflow.md) — TDD, git conventions, release rules
- [`docs/deployment.md`](docs/deployment.md) — tag-triggered release pipeline
