# Tap Counter

A minimal Android app that lets you count by tapping the back of your phone — or using on-screen buttons.

## Features

- **Back-tap detection** — Double-tap the back of your device while the app is open to increment or decrement the counter. Uses the accelerometer to detect sharp impulses, no special hardware required.
- **Count up / Count down** — Toggle between counting up and counting down with a single button group.
- **Manual controls** — `+1` and `-1` buttons for quick adjustments, plus a text field to jump to any starting count.
- **Reset** — One-tap reset back to zero with haptic feedback.
- **Haptic feedback** — Vibrates when the counter hits zero.
- **Material You** — Built with Material 3 components and theming.

## Getting Started

### Clone

```bash
git clone git@github.com:pkdiv/Tap-Counter.git
cd Tap-Counter
```

### Build & Run

Open the project in **Android Studio** and hit **Run**, or build from the command line:

```bash
./gradlew assembleDebug
```

Install the debug APK on a connected device:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Permissions

| Permission | Reason |
|---|---|
| `VIBRATE` | Haptic feedback when the counter reaches zero |

