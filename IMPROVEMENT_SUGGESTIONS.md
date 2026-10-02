# President Simulator — Improvement Suggestions & Roadmap

> Scan date: 2026-10-01
> Scope: 109 Kotlin files (~26k LOC main / ~1k LOC tests), 13 screens, 40+ subsystem
> states in a single `GameState`. Build compiles clean; test suite does **not**.
>
> Status legend: 🔴 blocking · 🟠 high-leverage · 🟡 medium · 🔵 long-term / idea

---

## 0. Scan summary

- `compileDebugKotlin` / `assembleDebug`: ✅ green.
- `testDebugUnitTest`: ❌ fails at **compile time** (corrupted file).
- With the corrupted file temporarily removed: **55 of 56 tests pass**, 1 fails and
  exposes a real data-loss bug (see §1.2).
- Uncommitted work in tree: 10 modified files, **+1,446 lines** (seeded determinism /
  `reseed()` work, screen expansions). Large coherent change sitting loose.

---

## 1. 🔴 Blocking issues (fix now)

### 1.1 Corrupted test file kills the entire suite
- **File:** `app/src/test/java/com/presidentsimulator/game/viewmodel/MonthlySimulationStressTest.kt`
- **State:** 4,937 NUL bytes, 0 lines. Kotlin emits 400+ syntax errors →
  `compileDebugUnitTestKotlin` fails → **zero tests run**.
- **Untracked in git** — no history to restore from. Must be rewritten.
- **Action:** Rewrite as a seeded golden-master stress test (see §4.2), or delete to
  unblock the suite and re-add later.

### 1.2 Save-validation bug: any JSON object loads as a fresh campaign
- **Failing test:** `SaveVersioningTest.malformedPayloadExplainsItselfInsteadOfThrowingSerializationNoise`
  on payload `{"something": 1}`.
- **Root cause:** `AnalyticsSaveViewModel.importGameStateFromJson()` uses
  `ignoreUnknownKeys = true` and every `GameState` field has a default, so
  `decodeFromJsonElement(GameState.serializer(), obj)` **succeeds** and produces a
  default state. No exception → load reported as *success* → player's campaign is
  silently replaced by a fresh default state when they import a wrong/corrupt file.
- **Fix options:**
  1. Require a marker key (e.g. `playerNation` or `vitals`) before decoding.
  2. Reject when `saveVersion == 0` **and** the decoded state deep-equals
     `GameState.initial()`.
  3. Both (defense in depth).

### 1.3 Audio subsystem ships with zero audio assets
- `GameAudioManager` references `R.raw.sfx_*` / `R.raw.bgm_*`, but `res/raw/` does not
  exist and `assets/` is empty. Every load logs "missing resource (skipped)".
- The README and Settings screen advertise music/SFX controls for a feature that is
  effectively dead code.
- **Action:** add assets (peace/war/crisis BGM + click/alert/war SFX), **or** hide the
  audio UI until assets exist. Don't ship promises the build can't keep.

### 1.4 Documentation drift
- `app-overview/APP_OVERVIEW_DETAILED.md` says `EVENT_CHANCE_PER_TICK = 0.15` and a
  fixed 1-second tick. Code: `EVENT_CHANCE_PER_TICK = 0.02f`, 12-month cooldown,
  speed modes (1,000ms / 333ms).
- README claims tests run — they currently can't (§1.1).
- **Action:** regenerate the overview or verify doc claims in CI.

---

## 2. 🟠 Engineering & architecture

### 2.1 No CI, no static analysis, no previews
- No `.github/`, no ktlint/detekt, zero `@Preview` functions, no instrumentation tests,
  `versionCode = 1`.
- **Action (in order):**
  1. GitHub Actions workflow: `testDebugUnitTest` + `assembleDebug` on every push/PR
     (would have caught §1.1 instantly).
  2. Add `lintDebug` to the workflow.
  3. ktlint or detekt with a ruleset tuned to the existing style.
  4. Bump `versionCode` as part of any release flow.

### 2.2 `GameViewModel` is a 1,348-line god object
- Owns state, tick loop, save/load, 100+ intent methods, and **Android Toasts**
  (`applyActionWithFeedback`).
- Success detection via reference identity (`if (new !== old) success = true`) — any
  engine that ever `copy()`s on a failed action reports **false success**.
- `AndroidViewModel` + Toasts makes the orchestrator untestable on the JVM.
- **Action:**
  - Replace Toasts with a `Channel<UiEvent>` consumed by the UI layer.
  - Engines return an explicit `ActionResult(success, message)` instead of relying on
    reference inequality.
  - Split intents by domain (`EconomyActions`, `MilitaryActions`, `SecurityActions`…)
    or extract a pure, JVM-testable turn coordinator the ViewModel merely forwards to.

### 2.3 Half-migrated naming: engines called `*ViewModel`
- `DiplomacyViewModel`, `TradeMarketViewModel`, `EspionageSecurityViewModel`,
  `GovernanceViewModel`, `ProductionLawViewModel`, `AdvancementViewModel`,
  `DemographicsCampaignViewModel`, `AnalyticsSaveViewModel` are **pure state→state
  engines**, not ViewModels. The project already has `FiscalEngine`, `VictoryEngine`,
  `DisasterEngine` etc.
- **Action:** rename to `*Engine` in one mechanical refactor commit (keep the git history
  clean; do it before more feature work lands on those files).

### 2.4 Recomposition & save pressure from a monolithic state flow
- One `StateFlow<GameState>` (40+ fields): every tick (1s normal / 333ms fast) rebuilds
  it and recomposes HUD + whole current screen.
- **Full JSON auto-save fires on every single tick** (`saveGameProgress()` inside
  `advanceTimeTick`), on `Dispatchers.IO` but still serializing a growing payload up to
  3×/second in FAST mode.
- **Action:**
  - Debounce auto-save: every N ticks + on pause + on `onStop()`/lifecycle background.
  - Expose per-domain `StateFlow`s (or `map`/`derivedStateOf` slices) so a screen
    subscribes only to what it renders.
  - Keep `collectAsStateWithLifecycle` everywhere (already good ✅).

### 2.5 Remote image dependency
- `NssCardImages` hotlinks Unsplash (`images.unsplash.com`) for ministry/unit/nation
  art; offline = gradient fallbacks across most of the UI. Hotlinking is fragile and
  arguably outside Unsplash's intended API use.
- **Action:** bundle the ~20 hero images in `assets/`, load with Coil from local first,
  remote as enhancement only. Also gives a path to store-gallery screenshots.

### 2.6 Uncommitted WIP (+1,446 lines)
- Seeded determinism (`campaignSeed`, `reseed()`, engine reconstruction) + screen
  expansions are all uncommitted.
- **Action:** get the suite green (§1), then commit this as its own focused change per
  the project's own git guidance ("keep commits focused by concern").

### 2.7 Persistence modernization
- SharedPreferences holds 4 full JSON blobs (automated save + 3 slots); payload size is
  shown in the UI (`KEY_LAST_PAYLOAD_BYTES`) but never bounded — a long campaign with
  dense logs could outgrow comfortable prefs handling.
- No `android:fullBackupContent` rules despite `allowBackup="true"`.
- **Action:** move payloads to app files (`filesDir/saves/`), keep prefs for settings;
  add backup rules; consider an explicit "Export campaign" (share/save-to-file) action.

### 2.8 Process-death & lifecycle safety
- Auto-save happens per tick; a player who acts and immediately gets killed mid-month
  loses that action (actions don't trigger a save).
- **Action:** save on `onStop()`/lifecycle pause, and after any player action batch
  (debounced).

---

## 3. 🟡 Platform reach, accessibility, store readiness

### 3.1 Portrait / phone support
- `MainActivity` is locked to `screenOrientation="landscape"` — casual phone users get
  a rotation nag on first launch.
- `NssResponsive.kt` already has breakpoint logic to build on.
- **Action:** design a portrait layout for the dashboard + HUD first (the rest can stay
  two-pane landscape), or at minimum gate landscape to tablets.

### 3.2 Accessibility
- **0** `R.string` references — every user-facing string is hardcoded in Kotlin.
- **24** `contentDescription = null` on icons.
- Font sizes down to 9sp in several cards (below comfortable touch/read thresholds).
- **Action:** start extracting strings to resources now (retrofitting 26k LOC later is
  ~5× harder); add content descriptions to meaningful icons; audit smallest font sizes.

### 3.3 Localization (blocked on §3.2)
- One locale today. String extraction is the prerequisite; then add a second locale as
  a proving ground (machine translation + native review).

### 3.4 Store / release readiness
- `versionCode = 1`, no signing config for release in `build.gradle.kts`.
- R8 enabled (`isMinifyEnabled`, `isShrinkResources`) ✅ — but **no release build has
  been exercised**; kotlinx-serialization + reflection-based `getIdentifier` audio
  lookup are classic R8 victims.
  - **Action:** run `assembleRelease` once and verify: `GameState.serializer()`
    survives, `resources.getIdentifier("sfx_…")` still resolves (R8 can strip unused
    raw names — consider generated res ids instead of string lookup).
- **Action:** add a release signing setup (env-var keystore, not committed) and a
  checklist item "build release APK" in CI.

### 3.5 Crash & quality telemetry
- No crash reporting or analytics pipeline beyond in-game `AnalyticsState`.
- Historical snapshots already exist and the overview doc calls out "add telemetry by
  piggybacking on analytics snapshots" — evaluate a crash/error reporting service
  when you're ready to integrate one (research options at integration time, don't pick
  from memory).

---

## 4. 🟡 Testing & balance safety

### 4.1 Coverage map (current: 56 tests across 9 files)
Covered: Fiscal, Event, War, Trade, Research, Domination, Campaign features, Save
versioning (+ the corrupted stress test).

**No tests for:** `PressDesk`, `CabinetEngine`, `OppositionEngine`, `DisasterEngine`,
`TermEngine`, `MandateEngine`, `AgendaBuilder`, `LoanEngine`, `StoryArcEngine`,
`LegacyLedger`, `SpeechEngine`, `PolicyImpactEngine`, `MilitaryIndustryEngine`,
`VictoryEngine.processMonth`, `MonthlySimulationPipeline` ordering.

- **Action:** prioritize a pipeline-order test (turn order is documented as the #1
  balance-regression risk zone) plus the politically-charged engines (cabinet,
  opposition, press) which drive approval swings.

### 4.2 Rewrite the stress test as a golden-master (replaces §1.1)
- Seeded runs (the new `campaignSeed` machinery makes this possible):
  - 500 months × N seeds; assert invariants (budget never NaN/overflow, approval ∈
    [0,100], population ≥ floor, gameOver eventually reachable, no engine throws).
  - Snapshot balance metrics (approval/budget trends) to detect drift between commits.

### 4.3 Property-based testing
- Engines are pure `state → state` functions — ideal for randomized property tests
  (e.g. "tax rate is always clamped to [MIN, MAX] after any action sequence").

### 4.4 UI previews & screenshot tests
- Zero `@Preview`s. Add previews for `NssComponents` primitives first (cheapest,
  highest reuse), then consider Compose screenshot testing in CI.

---

## 5. 🔵 Gameplay & product — "massive" improvements

### 5.1 Daily seed challenge (highest leverage — builds on in-flight work)
- `campaignSeed` + `reseed()` (already in the uncommitted diff) makes runs **exactly
  replayable**.
- Ship a **Daily Seed Challenge**: same seed for everyone, fixed nation, score =
  `LegacyLedger` pillars × `ScenarioCatalog` difficulty multiplier, local leaderboard
  now / global later.
- Near-zero new engine code; strong retention & shareability loop.

### 5.2 Event depth: chains and branches
- Current pool: **32 events**, mostly one-shot, 2%/tick with a 12-month cooldown.
- `StoryArcEngine` already supports multi-month arcs — reuse it to chain
  `border_incident → mobilization_doctrine → war_justification`, economic downturns,
  scandal cascades. Branching choices that change later event weights would transform
  the mid-game.
- Also: add state-aware events for systems that currently have none (opposition
  tactics, disasters, mandate failures).

### 5.3 Scenarios with real starting states
- 8 scenario packs + 4 challenges exist, but scenarios mostly set objectives while
  sharing one opening. `powder_keg` ("hostile neighbors, thin treasury, election in
  18 months") and `empty_granaries` ("food crisis on day one") **promise** deltas they
  don't deliver.
- **Action:** give each `ScenarioPack` actual starting-state deltas (budget, food,
  rivals, election timer, unrest) — pure data work in `ScenarioCatalog`.

### 5.4 Endgame scoring & replay
- Victory paths exist, but no post-game breakdown: final score, pillar chart, honors
  earned, rank/title, replay of key headlines (`NewsState` has the last 24 entries for
  free).
- `LegacyLedger` + scenario multiplier already compute most of it — assemble a
  **Debrief screen**.

### 5.5 Content breadth (current: 8 nations, ~15 techs, ~14 laws, 32 events)
- More playable nations (flag/drawable pipeline already exists: `CountryFlag`,
  `flag_*` drawables).
- Tech tree depth: 15 techs across 6 categories is thin for a long campaign; add
  tier-3 unlocks that change gameplay shape (not just modifiers).
- Laws: add mutually-exclusive pairs to force trade-offs (currently categories are
  independent checkboxes in practice).

### 5.6 Turn UX
- **Undo last turn**: engines are pure functions — snapshot `GameState` before
  `advanceTimeTick`, restore on undo (limit to 1–3 turns). Enormous quality-of-life for
  a strategy game.
- Turn summary screen exists (morning briefing) — consider a compact "month in
  review" delta card (budget ±, approval ±, war front changes).

### 5.7 Diplomacy & war texture
- Battle reports capped at 12 strings; consider a war log screen with per-battle
  detail (losses, terrain, dice rolls) — the data mostly exists in `WarModels`.
- Espionage missions resolve in a modal queue — add mission *intel* preview (risk vs
  reward odds already computed by `estimateMissionSuccess`).

### 5.8 Notifications & background play
- If auto-tick continues conceptually while backgrounded: WorkManager-driven
  "your nation's month advanced" notifications (opt-in). Otherwise ensure tick pauses
  cleanly on `onStop` (verify current behavior).

### 5.9 Haptics & game feel
- `GameAudioManager` SFX are dead (§1.3) — pair the audio fix with haptics
  (`HapticFeedbackType`) on tick, war declaration, and crisis resolution.

---

## 6. Suggested execution order

| Phase | Item | Why first |
|---|---|---|
| 1 | §1.1 Rewrite/delete corrupted test | Unblocks all testing |
| 1 | §1.2 Fix save-validation bug | Data-loss bug; suite goes green |
| 2 | §2.1 CI workflow | Locks in green suite |
| 2 | §1.3 Audio assets or hide feature | Stop shipping a broken promise |
| 2 | §2.6 Commit WIP determinism work | +1,446 lines at risk |
| 3 | §1.4 Doc refresh · §4.1–4.2 Pipeline/golden-master tests | Balance safety net |
| 3 | §2.2–2.3 ViewModel split + `*Engine` renames | Makes everything after cheaper |
| 4 | §2.4 Save debounce + state slicing · §2.7–2.8 Persistence | Perf & durability |
| 4 | §3.x Portrait, strings, a11y, release build | Reach & store readiness |
| 5 | §5.1 Daily seed challenge | Highest-leverage gameplay feature |
| 5 | §5.2–5.4 Event chains, scenario deltas, debrief screen | Depth & retention |

---

*Generated from a full codebase scan (build, tests, persistence, audio, UI, content).*
