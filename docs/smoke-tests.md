# Manual smoke-test checklist

Automated tests cover the pure logic (key mapping, zip extraction, layout
repository). The Android wiring below has to be checked on a device — run
through this list whenever an MVP slice lands.

Build first: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.
Install: copy to `/sdcard/Download`, open from a file manager (allow unknown sources).

## S1 — built-in keyboard works

The built-in keyboard is a dark Gboard-style QWERTY with three sheets,
no number row, no extra toolbars:

- **Letters**: `qwertyuiop` / `asdfghjkl` / `⇧ zxcvbnm ⌫` / `!#1 , ␣ . ↵`
- **Symbols 1/2**: `1234567890` / `! @ # $ % € & * ( )` / `2/2 - ' " : ; , ? ⌫` / `ABC , ␣ . ↵`
- **Symbols 2/2**: `+ × ÷ = < > { } [ ]` / `€ £ ₩ / ~ \` • ° ❤` / `1/2 _ \ | ¬ ¿ ¡ ⌫` / `ABC , ␣ . ↵`

1. Open the app → title "Keyboard HTML", status shows "not enabled".
2. Tap **Enable keyboard** → system screen shows "Keyboard HTML" toggle → turn it on.
3. Back in app → status shows "enabled".
4. Tap **Select keyboard** → picker lists "Keyboard HTML" → pick it.
5. Status shows "active".
6. Tap the test field → the dark keyboard appears (letters sheet).
7. Type a word; tap ⇧ once → next letter appears uppercase and auto-releases;
   double-tap ⇧ → caps lock (blue underline stays on) until tapped again.
8. Tap **!#1** → symbols 1/2: type `1 ! @ €` ...; tap **2/2 ↔ 1/2** → the second symbol sheet swaps; tap **ABC** → back to letters.
9. ⌫ deletes, ␣ inserts space, ◦↵ submits the field.

Not yet (special keys are visual-only for now): shift caps-lock/autocaps,
long-press accents, voice/clipboard bar, theme variants.

## S2 — shortcuts behave

- **Enable keyboard** opens the system input-method settings.
- **Select keyboard** opens the input-method picker.
- Status text updates when returning to the app after enabling/selecting.

## S3 — zip import

1. The build already drops ready-made samples beside the APK in `app/build/outputs/apk/debug/`: `red-test.zip` ("Red Test", letters R/E/D) and `mint-test.zip` ("Mint Numbers", digits 0-9).
2. Tap **Import keyboard (.zip)**, pick `red-test.zip` in the picker.
3. Toast: "Imported and selected `Red Test`" (mentioning the manifest name).
4. The keyboard (on any field) now renders the red layout.
5. Import `mint-test.zip` the same way; the keyboard switches to it.
6. Import both again? Each file name owns ONE layout folder: importing different zips adds layouts to the chooser; importing the same zip replaces only its own layout. The chooser and toasts show the manifest `name` (no id suffix), falling back to the zip file name.

## S4 — choose among installed layouts

1. Import a second layout zip.
2. Tap **Choose layout** → dialog lists both layouts alphabetically.
3. Pick one → toast "Selected layout …" and an already-open keyboard reloads
   the chosen HTML immediately (no close/reopen needed).
4. **Choose layout** with no imported layouts → toast "No imported layouts yet".
5. Re-importing the same zip name replaces the previous version; a live
   keyboard reloads the new page content too (not only the selection).

### Building a test layout zip

Sample zips ship with the build (`red-test.zip`, `mint-test.zip` next to the APK). To roll your own: any zip with `index.html` at root works. Optional `manifest.json` gives the keyboard a display name (otherwise the zip file name is shown):

```text
my-keeb.zip
  ├─ index.html      ← required
  ├─ manifest.json   ← optional: {"name": "My Keeb"}
  ├─ kbd.css         ← optional
```

### Notes

- **Enter** sends a plain hardware-style ENTER event (does not force `actionDone`-style submit); selection-aware behavior is planned.
- **Backspace** clears the current selection in one step, otherwise deletes one character to the left.
- The chooser always offers **Default (built-in)** to return to the bundled QWERTY.

Minimal `index.html` for testing (red background proves it loads):

```html
<!DOCTYPE html>
<html>
<head><meta name="viewport" content="width=device-width, user-scalable=no">
<style>body{margin:0;background:#8b1e1e;display:flex}
button{flex:1;height:46px;font-size:18px}</style></head>
<body>
  <button onclick="KeyboardBridge.onKey(this.textContent)">a</button>
  <button onclick="KeyboardBridge.onKey('space')">␣</button>
  <button onclick="KeyboardBridge.onKey('backspace')">⌫</button>
</body>
</html>
```
