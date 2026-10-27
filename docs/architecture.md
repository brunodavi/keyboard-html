# Architecture & project requirements

keyboard-html is an experimental Android on-screen keyboard app. Objective: fully user-customizable keyboards **without recompiling the APK**.

## Design decisions

- Keyboard UI is **HTML rendered in a WebView**; the web layer talks to the Android keyboard service through a **JS↔Android API** (JavascriptInterface or equivalent). Layouts become ordinary web files anyone can author.
- Keyboard layouts are user-importable **.zip packages**, extracted into the app's **internal storage** — never use broad storage permissions; use the system file manager via intent (SAF) for picking the zip.
- Default bundled keyboards (QWERTY etc.) ship as HTML assets inside the APK.
- The app must **work fully offline** — no network dependencies at runtime.
- The app's configuration screen is a native XML layout; the keyboard view stays an HTML WebView under an IME service (`docs/smoke-tests.md` covers the on-device checks).

## Why this shape

- Recompiling an APK for every layout/theme tweak is the pain this project exists to remove; HTML packages transfer the customization burden to plain web files.
-WebView keeps the keyboard rendering ecosystem simple (CSS layout, JS key logic) while Android keeps IME/service responsibilities native.
- Internal storage + SAF import keeps the permission model minimal (no READ_EXTERNAL_STORAGE).

## Module layout

Single module `app`, plain Java (no Kotlin, no Compose). Entry point `com.keyboardhtml.MainActivity`. The app's configuration screen is a standard Android **XML layout** (`res/layout/activity_main.xml`) for maintainability and styling; the keyboard itself stays an HTML WebView keyed through the JS bridge. App identity/versions/SDK levels all live in `gradle.properties` — see `docs/build.md`.
