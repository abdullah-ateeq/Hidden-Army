# 🛡️ Hidden Army

**Hidden Army** is an offline, hidden-information tactical turn-based board game for Android built with **100% Jetpack Compose** and **Material 3**. 

Strategic decision-making, fog-of-war bluffs, tactical movement, and base occupation define the core gameplay against an adaptive CPU opponent.

---

## 🎮 Game Overview

In **Hidden Army**, you command a squad of 5 hidden units on a **7x7 tactical grid**. Combat reveals unit identities and power levels permanently, turning early skirmishes into tense information gathering and tactical maneuvering.

### 🏆 Win Conditions
You win the battle by achieving either of the following objectives:
1. **Total Elimination**: Destroy all 5 enemy units.
2. **Base Dominance**: Control all **3 neutral bases** on the battlefield at the beginning of your turn.

---

## ✨ Key Features

- 🕵️ **Fog of War Mechanics**: Units start with hidden identity. Combat permanently exposes unit types and power ratings for both attacker and defender.
- ♟️ **7x7 Tactical Grid**: Strategic placement and movement around 3 central capture bases (Row 3, Columns 1, 3, 5).
- 🧠 **Smart AI Opponent**: Play against a customizable CPU featuring **Easy**, **Medium**, and **Hard** difficulty levels.
- 📊 **Detailed Match Statistics**: Tracks total games played, win rate, win streak, total units eliminated, bases captured, and perfect victories.
- 🎨 **Modern Material 3 Design**:
  - System, Light, Dark, AMOLED Dark, and Tactical Military themes.
  - Dynamic Color theme support (Android 12+).
  - Smooth combat animations, battle overlays, and interactive sound & haptic feedback.
- ⚡ **100% Offline Gameplay**: No internet connection required.

---

## 🎖️ Army Hierarchy & Unit Types

Each player commands 5 units consisting of the following hierarchy:

| Unit Icon | Unit Type | Power | Movement | Description |
| :---: | :--- | :---: | :---: | :--- |
| 🏹 | **Scout** | `1` | **2 Spaces** | Highly mobile unit capable of moving up to 2 orthogonal spaces. Ideal for quick recon and base capping. |
| 🗡️ | **Soldier** | `2` | **1 Space** | Standard frontline infantry. Moves 1 space orthogonally. |
| 🛡️ | **Captain** | `3` | **1 Space** | Heavy assault officer. Outranks Soldiers and Scouts. |
| ⭐ | **Commander** | `4` | **1 Space** | Ultimate field officer. The most powerful unit on the board. |

### ⚔️ Combat Rules
- **Attacking**: Move into an adjacent enemy cell (1 space orthogonally).
- **Higher Power Wins**: The unit with greater power destroys the defender and occupies its tile.
- **Draw (Equal Power)**: Both units are destroyed and removed from the board.
- **Permanent Reveal**: Combat reveals both unit identities on the field permanently.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture**: Clean MVVM (Model-View-ViewModel) with Kotlin `StateFlow`
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **State & Storage**: SharedPreferences with Kotlin Flow wrapper (`GamePreferences`)
- **Compatibility**: Min SDK 24 (Android 7.0+) | Target SDK 37 (Android 16)

---

## 📁 Project Structure

```text
com.example.hiddenarmy
├── ai/              # AI CPU strategy engine (HiddenArmyCpuStrategy)
├── data/            # Local preferences & user statistics (GamePreferences)
├── logic/           # Pure game engine logic & rules (HiddenArmyGameEngine)
├── model/           # Game domain models (Unit, UnitType, Position, GameState, etc.)
├── navigation/      # Navigation routes & graph definition (AppNavigation)
├── ui/
│   ├── components/  # Reusable UI elements (BoardView, UnitToken, BattleOverlay, etc.)
│   ├── screens/     # App screens (HomeScreen, GameScreen, ResultScreen, SettingsScreen, AboutScreen)
│   ├── theme/       # App typography, color palettes, and themes
│   └── viewmodel/   # HiddenArmyViewModel driving UI state
└── util/            # Audio & haptic feedback helpers (SoundAndHaptics)
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Version 11 or higher
- **Android SDK**: API 37 (Android 16)

### Building & Running
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/Hidden-Army.git
   cd Hidden-Army
   ```
2. Open the project in **Android Studio**.
3. Build and run on an emulator or connected device:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
