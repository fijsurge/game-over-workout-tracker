Game Over Tracker

A high-intensity workout tracker based on the Game Over program.

Features

Phase Logic: Automatically adapts targets for Phase 1 (Conditioning), Phase 2 (Growth), and Phase 3 (Strength).

Strength Analytics: Tracks Peak Weight and Average Weight per exercise.

PWA Ready: Installable on iOS and Android via browser.

How to use this repository

This is a single-file application located in index.html.

It uses CDNs for React and Tailwind CSS, so no npm install is required.

Customizations Made

Set counts and exercises have been manually adjusted to match user preferences.

Phase 3 includes specific exercises like Deadlifts and Weighted Dips.

Progress visualization focuses on weight progression rather than just volume.

## Wear OS Watch App

The companion watch app (`android/wear/`) runs on Wear OS 5+ and provides:
- Phase/day/exercise selection on the watch
- Per-set weight and reps logging with keypad entry
- Live session timer and heart rate display
- 60-second rest countdown between sets
- Workout save that syncs back to the phone app

### Deploying to the watch

```bash
cd android && ./gradlew :wear:assembleDebug
adb -s <watch-ip>:<port> install -r wear/build/outputs/apk/debug/wear-debug.apk
```

### Permissions (required after every fresh install)

```bash
# Grant BODY_SENSORS via ADB — Wear OS 6 does not always show this in the permission dialog
adb -s <watch-ip>:<port> shell pm grant com.gameovertracker.app android.permission.BODY_SENSORS
```

The `health.READ_HEART_RATE` permission is requested automatically at first launch via the system dialog — tap **Allow** when it appears. Both permissions survive app updates; re-grant is only needed after a full uninstall or `pm clear`.

### ADB WiFi pairing (Pixel Watch 4)

1. On the watch: Settings → Developer options → Wireless debugging → Pair new device
2. `adb pair <ip>:<pairing-port>` (enter the 6-digit code)
3. `adb connect <ip>:<connection-port>`

## Migrating to a new phone

Google's "copy apps and data" phone transfer **will not** bring this app or its history over — it only restores apps found in Play Store for your account, and this is a sideloaded debug APK. There's no export/import feature, so the workout history has to be copied manually via ADB while you still have the old phone.

The data lives in the Capacitor WebView's localStorage, at `app_webview/Default/Local Storage/leveldb` inside the app's private storage — accessible via `run-as` because it's a debug build.

1. Install the app on the new phone and launch it once (creates the `app_webview` folder), then force-stop it:
   ```bash
   adb -s <new-ip>:<port> install -r android/app/build/outputs/apk/debug/app-debug.apk
   adb -s <new-ip>:<port> shell monkey -p com.gameovertracker.app -c android.intent.category.LAUNCHER 1
   adb -s <new-ip>:<port> shell am force-stop com.gameovertracker.app
   ```
2. On the **old** phone, archive the data folder:
   ```bash
   adb -s <old-ip>:<port> exec-out run-as com.gameovertracker.app tar -cf - "app_webview/Default/Local Storage" > localstorage.tar
   ```
3. Base64-encode it locally and stream it onto the new phone — `adb shell "cmd" < file` silently truncates large binary payloads, so base64 (text-safe) is required:
   ```bash
   base64 -w 0 localstorage.tar > localstorage.b64
   adb -s <new-ip>:<port> shell "run-as com.gameovertracker.app sh -c 'base64 -d > app_webview/tmpfile.tar'" < localstorage.b64
   ```
4. Extract it **from the app's root dir, with no `-C` flag** (the archive's paths already start with `app_webview/...`; adding `-C app_webview/Default` double-nests it). Note the app's top-level data dir itself is write-protected against `run-as` — this only works because we're writing inside the already-writable `app_webview/` subdirectory:
   ```bash
   adb -s <new-ip>:<port> shell "run-as com.gameovertracker.app sh -c 'rm -rf \"app_webview/Default/Local Storage\" && tar -xf app_webview/tmpfile.tar && rm app_webview/tmpfile.tar'"
   ```
5. Re-grant `BODY_SENSORS` on the new phone (see [Permissions](#permissions-required-after-every-fresh-install) above) and relaunch the app to confirm the history/stats loaded before wiping the old phone.

Multi-word paths (like `Local Storage`) must be quoted *inside* a `sh -c '...'` string — `adb shell` joins separate local arguments with spaces before sending them, so local quoting alone doesn't survive the trip.

## Test Data

To generate sample workout data for testing the interface and charts:

1. Open `index.html` in a browser
2. Open the developer console (F12)
3. Paste the contents of `test-data.js` and press Enter
4. Refresh the page

This generates 8 weeks of realistic workout history across all 3 phases with progressive overload, fatigue drop-off, and natural set-to-set variance. The most recent session is pre-loaded as current log inputs.
