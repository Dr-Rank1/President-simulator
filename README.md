# President Simulator

A UI-heavy, menu-driven grand strategy management simulator for Android. Built with **Kotlin**, **Jetpack Compose**, and **MVVM** (`StateFlow`).

Manage a nation's budget, ministries, military, diplomacy, laws, research, espionage, trade, and global governance through a dense command-center dashboard — inspired by *Modern Age 2* (Oxiwyle) and styled after the **Nation State Simulator** UI design reference.

## Features

- **Landscape-only** gameplay optimized for tablets and phones in horizontal mode
- **Dark geopolitical dashboard** — vitals HUD, sidebar ministry navigation, hero banners, image-rich cards
- **Monthly simulation tick** with manual advance, pause, and auto-play
- **Ministries**: Economy, Defense, Foreign Affairs, Domestic Policy, Intelligence, Science, UN, and more
- **Bulk actions** — build infrastructure, recruit troops, and purchase hardware with `1x | 10x | Max` controls
- **Event & crisis engine** — random events pause the game loop until resolved
- **Save / load** — full game state serialized to JSON via `SharedPreferences`
- **Audio** — background music with crossfade and sound effects

## Architecture

| Layer | Responsibility |
| --- | --- |
| `data/` | Immutable `GameState` snapshot and domain models (economy, military, diplomacy, production, laws, research, espionage, trade, governance) |
| `viewmodel/` | `GameViewModel` owns `StateFlow<GameState>`, monthly tick, and all player actions |
| `ui/` | Compose screens, NSS design components, navigation shell, and theme |
| `audio/` | `GameAudioManager` for BGM and SFX |

Simulation advances **one month per tick** via the HUD time controls or **auto-play**.

## Requirements

- Android Studio Ladybug or newer
- JDK 17
- Android SDK 35 (API 26+ device/emulator)
- `local.properties` with `sdk.dir` pointing to your Android SDK

## Build & Install

```bash
# From project root
./gradlew assembleDebug

# Install to a connected device
./gradlew installDebug
```

Or open the project in Android Studio, sync Gradle, and run the **app** configuration.

## Controls

| Control | Action |
| --- | --- |
| **Pause / Play / Fast-forward** | Top HUD — pause auto-tick, resume, or advance one month |
| **Sidebar** | Switch between ministries (Economy, Defense, Foreign Affairs, etc.) |
| **Ministry tabs** | Each ministry has tabbed sub-panels (e.g. Sectors, Policy, Budget, Trade) |
| **Cards & sliders** | Adjust policies, invest in sectors, recruit units, manage diplomacy |
| **Alerts panel** | Live warnings shown in the sidebar (wars, shortages, coup risk) |

## Project Structure

```
app/src/main/java/com/presidentsimulator/game/
├── MainActivity.kt              # Entry point (landscape)
├── data/                        # GameState and domain models
├── viewmodel/                   # GameViewModel and feature extensions
├── audio/                       # BGM / SFX engine
└── ui/
    ├── navigation/              # GameNavigation shell
    ├── components/              # GlobalHud, NssComponents, cards, dialogs
    ├── screens/                 # Ministry screens
    └── theme/                   # NSS dark color scheme and typography
```

## UI Design Reference

The visual design is based on the **Nation State Simulator UI Design** reference (Figma/React). The Android app replicates the dark command-center aesthetic using Jetpack Compose — gradient banners, sector/unit/nation cards, monospace vitals, and bordered panels.

## Campaign and Turn Flow

- Each scenario lists its campaign objectives during setup and tracks objective progress on the command dashboard.
- The Standard Mandate includes a first-month guide for budget, public support, and research decisions.
- The dashboard estimates monthly fiscal balance and, during a deficit, how many months current reserves could cover.
- Monthly bulletins report fiscal movement and likely approval pressures alongside major political and world events.
- Resolved random crises have a short cooldown before another random event can interrupt the campaign. Persistent disasters and other active consequences continue on their own timeline.
- The persistent **More** navigation menu links to Science, Domestic Policy, the UN, Cabinet, Demographics, Analytics, and Settings.

The monthly simulation order is documented in `MonthlySimulationPipeline.kt`: production and fiscal settlement, foreign affairs and security, domestic political systems, long-term progression, and finally history and agenda generation.

Campaigns also support optional challenge rules that modify the starting conditions and final score multiplier. Political story arcs unfold over multiple months, with choices affecting the press, cabinet, opposition, and legacy record. The dashboard includes a regional relationship map and active story progress; morning briefings surface the current storyline and a state-aware chief-of-staff outlook. Campaign end reports show the five legacy pillars and honors earned.

## License

Private project — see repository owner for terms.
