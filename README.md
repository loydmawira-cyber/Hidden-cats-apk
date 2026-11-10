# Hidden Cats: Color Logic

[![Android CI](https://github.com/example/hiddencats/actions/workflows/android.yml/badge.svg)](https://github.com/example/hiddencats/actions/workflows/android.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-green.svg)](https://developer.android.com/jetpack/compose)

A logic puzzle Android game where players locate hidden cats on a vibrant colored grid using procedural deduction.

---

## 🎮 Game Concept & Rules

Cats are hiding across a grid divided into distinct colors. Indicators at the edge of each row and column indicate the exact number of cats in that line.

### Core Rules
1. **Adjacency:** Cats are territorial. Two cats can **never touch** horizontally, vertically, or diagonally (8-neighbor restriction).
2. **Color Uniqueness:** Every active color on the board hides exactly **one cat across the entire puzzle**. This means puzzles with more colors contain more cats; no color can contain a second cat.
3. **Line Counts:** The number of cats placed in each row and column must exactly equal the row and column clue numbers.
4. **Marking States:**
   - ❓ **Unknown:** Cell has not yet been marked.
   - 🐾 **Possible:** Note/candidate marker for candidate cells.
   - ✖️ **Empty:** Marked as confirmed empty.
   - 🐱 **Confirmed Cat:** Cat placed here.

---

## 🧩 Modes & Difficulties

- **Classic Mode:** Traditional puzzle solving with 3 lives and mistakes penalty.
- **Timed Mode:** Race against a countdown clock tailored to board size and difficulty.
- **Daily Challenge:** Unique deterministic daily puzzle seeded by UTC calendar date.
- **Relaxed Mode:** Zen play without timer or life penalties.

**Supported Board Sizes:** 4×4, 5×5, 6×6, 7×7, 8×8, 9×9  
**Difficulty Levels:** Easy, Normal, Hard, Expert

---

## 🏗 Architecture & Tech Stack

- **Modern UI:** Jetpack Compose with Material 3 theming, adaptive layouts, and dynamic light/dark styling.
- **Architecture:** Clean MVVM with Kotlin Coroutines and StateFlow.
- **Game Engine:** Standalone procedural `PuzzleGenerator` with backtracking `PuzzleSolver`, deductive `HintEngine`, and move history undo/redo stack.
- **Persistence:**
  - **Room Database:** Local game records, history, streaks, and statistics.
  - **Jetpack DataStore:** Player preferences (audio, haptic, dark mode, color-blind mode, left-handed layout).
- **Cloud Backend:**
  - **Firebase Authentication:** Anonymous sign-in with seamless offline fallback.
  - **Cloud Firestore:** Remote stats sync, streak backup, and leaderboards.
- **In-App Billing:** Google Play Billing Library 8.3.0 (Premium Unlock, Ad Removal, Hint Packs) with full simulated test harness.
- **Accessibility:** Color-blind pattern overlays, 48dp touch targets, TalkBack semantics, high-contrast markers.

---

## 🛠 Building & Running

### Requirements
- Android Studio Ladybug or later / JDK 17
- Android SDK 36 (minSdk 24)

### Terminal Commands

Run all JVM unit and Robolectric tests:
```bash
./gradlew testDebugUnitTest
```

Build the debug APK:
```bash
./gradlew assembleDebug
```

Build release bundle (AAB):
```bash
./gradlew bundleRelease
```

---

## 🔒 Firebase Configuration

1. Place your `google-services.json` in the `app/` folder (refer to `google-services.json.example`).
2. Deploy the provided Firestore security rules:
   ```bash
   firebase deploy --only firestore:rules
   ```

---

## 📄 License & Policies

- [License](LICENSE) (MIT)
- [Privacy Policy](PRIVACY_POLICY.md)
- [Terms of Service](TERMS_OF_SERVICE.md)
- [Contributing Guidelines](CONTRIBUTING.md)
