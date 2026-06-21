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

## Test Data

To generate sample workout data for testing the interface and charts:

1. Open `index.html` in a browser
2. Open the developer console (F12)
3. Paste the contents of `test-data.js` and press Enter
4. Refresh the page

This generates 8 weeks of realistic workout history across all 3 phases with progressive overload, fatigue drop-off, and natural set-to-set variance. The most recent session is pre-loaded as current log inputs.
