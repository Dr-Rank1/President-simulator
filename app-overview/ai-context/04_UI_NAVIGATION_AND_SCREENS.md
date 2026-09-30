# UI Navigation and Screens

## Active Navigation Surface

Navigation destinations live in `ui/navigation/GameDestination.kt`.
Route composition is defined in `ui/navigation/GameNavigation.kt`.

### Active destinations

- `dashboard` -> `MainDashboardScreen`
- `economy` -> `EconomyScreen`
- `military` -> `MilitaryScreen`
- `diplomacy` -> `DiplomacyScreen`
- `secret_service` -> `SecurityScreen`
- `science` -> `ScienceScreen`
- `laws_society` -> `LawsScreen` (laws + SOCIETY funding/religion/universities tab)
- `governance` -> `GovernanceUNScreen`
- `audio_settings` -> `SettingsAudioScreen` (audio + save/load)
- `analytics` -> `AnalyticsScreen` (charts + save/load)
- `demographics` -> `ApprovalDemographicsScreen`

Bottom nav stays Overview / Economy / Defense / Foreign / Intel. Science, Domestic, UN, Settings, Analytics, and Demographics are reached from dashboard tiles (and Settings/Analytics for saves).

### Launch gate

When `showLaunchScreen` is true, `GameNavigation` shows `LaunchScreen` (Continue / New Game / slot loading) instead of the HUD shell. "New Game" opens `NewGameSetupScreen`, a 6-step wizard: nation -> leader title -> ideology -> state religion -> victory path -> difficulty. Back press steps backward through the wizard.

### Global overlays (priority)

1. `EventCrisisDialog` (active crisis)
2. `ElectionNightDialog` (pending election night)
3. War-end branch: `ConquestChoiceDialog` when `WarOutcome.conquestAvailable` (mandatory: scrim/back cannot dismiss), else `WarOutcomeDialog`
4. `MissionResultDialog` (queued covert outcomes)
5. Campaign end dialog (coup loss, election loss, or victory) with Load Save + Return to Title

---

## Screen Roles

## `MainDashboardScreen`

- `NewsTicker` (rotating world news) + WAR banner
- `DominancePanel` (victory-path progress bar)
- REALM territory strip (conquered nations)
- Hero country header and vitals cards
- Quick ministry jump tiles (including Analytics / Demographics)

## `EconomyScreen`

- Sector Invest builds factories/farms/housing/power/mines
- Tax policy slider
- Live Trade tab: tariffs, spot market, deals, propose contract

## `MilitaryScreen`

- Forces / recruitment / logistics
- Deployment posture + salary funding on Logistics

## `DiplomacyScreen`

- Rival cards, grain export, trade/NAP treaties, alliances, war

## `SecurityScreen`

- Internal security metrics and covert ops

## `ScienceScreen`

- Research progress and tech tree

## `LawsScreen`

- Law catalogs by tab + SOCIETY ministry/religion/university controls

## `GovernanceUNScreen`

- UN assembly, bribes, alliances

## `AnalyticsScreen` / `ApprovalDemographicsScreen`

- History charts + manual save/load
- `WorldStandingsPanel`: army/economy ranks, nations controlled, wars won, top-5 rival strengths
- Persistent approval cohorts and election year

## `EconomyScreen` / `MilitaryScreen` extras

- Economy: central-bank loan desk (BORROW max / REPAY debt/10) via `LoanEngine`
- Military: military-industry build row (arsenals / airfields / shipyards)

## `SettingsAudioScreen`

- Music/SFX + optional save/load panel

## `LaunchScreen`

- Title Continue / New Game entry

---

## NSS UI Component System

Primary reusable components live in:

- `NssComponents.kt`
- `NssPhotoHeader.kt`
- `NssCardImages.kt`

Important shared pieces:

- `NssScreenHeader`
- `NssPanel`
- `NssGameBar` / `NssXpBar`
- `NssBadge`
- `NssPhotoHeader`
- `MinistryBottomNav`
- `GlobalHud`
- `ConquestChoiceDialog` / `NewsTicker` + `DominancePanel` / `MissionResultDialog`

---

## Image System Notes

- Major cards/headers use remote image URLs from `NssCardImages`.
- `NssPhotoHeader` handles load/fallback/scrims through Coil with standardized `PhotoScrimAlpha` presets.

---

## Legacy / Non-primary UI Files

Older or parallel screens live in `ui/legacy/` and are not wired into active nav:

- `MainGameScreen`
- `AnalyticsDashboardScreen`
- `TradeLogisticsScreen`
- `ProductionLawScreen`
- `MilitaryDiplomacyScreen`
- `AdvancementSocietyScreen`
- `EspionageSecurityScreen`

When editing, verify whether a target screen is wired in `GameNavigation` before investing major effort.
