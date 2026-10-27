# Manual smoke-test checklist

Automated tests cover the pure logic (key mapping, zip extraction, layout
repository). The Android wiring below has to be checked on a device — run
through this list whenever an MVP slice lands.

Build first: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.
Install: copy to `/sdcard/Download`, open from a file manager (allow unknown sources).

## S1 — built-in keyboard works

1. Open the app → title "Keyboard HTML", status shows "not enabled".
2. Tap **Enable keyboard** → system screen shows "Keyboard HTML" toggle → turn it on.
3. Back in app → status shows "enabled".
4. Tap **Select keyboard** → picker lists "Keyboard HTML" → pick it.
5. Status shows "active".
6. Tap the test field → the dark HTML keyboard appears.
7. Letters type into the field correctly, space inserts a space.
8. BKSP deletes one character, ENTER closes the field/submits.

## S2 — shortcuts behave

- **Enable keyboard** opens the system input-method settings.
- **Select keyboard** opens the input-method picker.
- Status text updates when returning to the app after enabling/selecting.

## S3 — zip import

1. Prepare a test layout zip (see below), then tap **Import keyboard (.zip)**.
2. Picker opens, select the zip.
3. Toast: "Imported and selected `<name>`".
4. The keyboard (on any field) now renders the imported layout.

## S4 — choose among installed layouts

1. Import a second layout zip.
2. Tap **Choose layout** → dialog lists both layouts alphabetically.
3. Pick one → toast "Selected layout …", next keyboard use shows it.
4. **Choose layout** with no imported layouts → toast "No imported layouts yet".
5. Re-importing the same zip name replaces the previous version.

### Building a test layout zip

Any zip with `index.html` at root works. Optional `manifest.json` gives the keyboard a display name (otherwise the zip file name is shown):

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
