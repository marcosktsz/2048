# Material 2048 for Android

A native Android 2048 game built with Kotlin, Jetpack Compose, and Material 3 Expressive. Low-value tiles use dynamic system accent roles; higher-value tiles use distinct, muted light/dark swatches. Tiles slide and merge with expressive spatial springs, and score changes get a quick count-up and subtle scale pop. Settings lets players choose system/light/dark themes and toggle haptic feedback.

The project uses the Compose alpha BOM to access Material 3's expressive motion APIs.

## Screenshots

| Game | Settings | Game over |
|:---:|:---:|:---:|
| ![Game board, light theme](docs/screenshots/01-board-light.png) | ![Settings, light theme](docs/screenshots/03-settings-light.png) | ![Game over, light theme](docs/screenshots/05-game-over-light.png) |
| ![Game board, dark theme](docs/screenshots/02-board-dark.png) | ![Settings, dark theme](docs/screenshots/04-settings-dark.png) | ![Game over, dark theme](docs/screenshots/06-game-over-dark.png) |

## Build

```sh
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Play

Swipe in any direction to move the board. Use **Undo** to reverse the last move, or **New Game** to start over. The board, score, undo state, and best score persist across app restarts.

Run the game-logic tests with `./gradlew testDebugUnitTest`.
