# Domain State Models

## Root: `GameState`

Defined in `data/Models.kt`, `GameState` contains:

- Time: `month`, `year`
- `vitals`
- `economy`
- `military`
- `diplomacy`
- `production`
- `legal`
- `analytics`
- `internalSecurity`
- `espionage`
- `gameOver`
- `research`
- `society`
- `trade`
- `market`
- `governance`
- `territory` (domination: conquered nations)
- `victoryPath` (chosen path + world-spread progress)
- `militaryIndustry` (arsenals / airfields / shipyards)
- `setup` (new-game wizard record: leader title, ideology, religion)
- `news` (rolling world news ticker, max 24 entries)

Computed cross-system fields:

- `netIncome`
- `effectiveCombatStrength` (multiplied strength: tech/religion/cabinet/perk/embargo)
- `effectiveMilitaryStrength` (combat strength + industry flats + puppets*40)
- `effectiveProductionMultiplier`
- `tradeExportBonus`
- `tributeIncome`

## Initial Seed

`GameState.initial()` bootstraps a playable nation:

- Budget: 50B
- Approval: 55
- Population: 50M
- Economy/military/production prefilled with non-zero assets

---

## Economy + Vitals

### `VitalsState`

- `budget: Long`
- `approval: Float`
- `population: Long`

### `EconomyState`

- Tax rate, exports/imports, factories/farms/housing
- Methods:
  - `taxRevenue(population)`
  - `totalRevenue(population)`
  - `netIncome(population)`
- Derived:
  - `effectiveExports`
  - `effectiveImports`
  - `upkeep`
  - `totalExpenses`

---

## Military + Diplomacy

### `MilitaryState`

- Personnel and hardware (`tanks`, `jets`, `ships`, `nuclearArsenal`)
- `salaryFunding` multiplier
- `deployment`
- `defcon`
- Computed:
  - `morale`
  - `monthlyUpkeep`
  - `combatStrength`

### `DiplomacyState`

- `rivals: List<RivalNation>`
- `diplomaticInfluence`
- `activeWar: WarState?`

### `RivalNation`

- Relation score, military strength, treaty booleans
- Stance computed from relation score

### `WarState`

- `warProgress` in [-100, +100]
- casualty counters
- `monthsActive`

---

## Production + Laws

### `ProductionState`

Stocks:

- `energy`, `food`, `materials`, `goods`

Infrastructure:

- `powerPlants`, `mines`

Per-tick accounting fields:

- produced/consumed/sold/revenue metrics
- shortage flags (`energyShortage`, `foodShortage`)

### `LegalState`

- `ideology`
- `activeLawIds`
- computed aggregate modifiers:
  - approval, production, food demand, energy demand
- total upkeep

### `Law`

Policy object with:

- activation/upkeep costs
- approval threshold
- multiple systemic modifiers

`LawCatalog` contains all law definitions by category.

`Ideology` enum: DEMOCRACY, AUTOCRACY, COMMUNISM (setup wizard picks one and mirrors it into `legal.ideology`).

---

## Science + Society

### `SocietyState`

- health/education/culture levels (0-100)
- ministry funding sliders (0-1)
- religion
- university count
- last science generated

### `StateReligion`

Values: SECULAR, TRADITIONAL, STATE_CULT, PLURALIST.

Provides:

- approval bonus
- instability modifier
- multipliers (science, military, production)

The setup wizard choice is mirrored into `society.stateReligion`.

### `ResearchState`

- science points
- unlocked tech IDs
- active research
- progress
- extra funding tier

`TechCatalog` defines technologies with prerequisites and effect modifiers.

---

## Security + Espionage + End State

### `InternalSecurityState`

- `instabilityScore` (0-100)
- `coupRisk` (0-100)
- active protocols list
- computed upkeep and suppression totals

### `EspionageState`

- `spyCount`
- `intelligencePoints`
- mission list
- computed available spies and active count

### `CovertMission`

- mission type, target, success probability
- required ticks/progress
- status (`ACTIVE`, `SUCCESS`, `FAILED`)

### `GameOverState`

- simple terminal marker + reason string

---

## Trade + Market

### `TradeState`

- active deals
- trade balance
- last tariff revenue
- tariff rate

### `MarketState`

- live `MarketResource` quote list
- lookup/update helpers by commodity

### `TradeDeal`

- import/export contract
- amount per tick, price per unit, ticks remaining

---

## Governance

### `GlobalGovernanceState`

- active UN resolution
- alliance list
- diplomatic influence
- global policy toggles:
  - nuclear embargo
  - global tax
  - peacekeeping
  - weapons ban
- last resolution result text

### `UNResolution`

- type, proposer, optional target
- votes-for and votes-against country lists
- remaining voting time

### `Alliance`

- id/name/leader/members/shared defcon

---

## Domination Models (`TerritoryAndVictoryModels.kt`)

### `TerritoryState`

- `conquered: List<ConqueredTerritory>` (countryId, name, status, monthsHeld)
- `controlledCount` (annexed + puppet)
- `puppetCount`
- `monthlyTributeIncome` ($1.2B per puppet)
- `annexationInstabilityPressure`

### `TerritoryStatus`

- ANNEXED / PUPPET / LIBERATED

### `VictoryPath` + `VictoryPathState`

- MILITARY_DOMINANCE / RELIGIOUS_DOMINANCE / IDEOLOGICAL_DOMINANCE
- `VictoryThresholds`: NATIONS_TO_CONTROL=4, INFLUENCE_TO_WIN=85
- `progressFraction(controlled)` for UI bars

### `MilitaryIndustryState` + `MilitaryFacilityType`

- ARSENAL ($8B, +2 tanks/mo), AIRFIELD ($10B, +1 jet/mo), SHIPYARD ($12B, +1 ship/mo)

### `SetupState`, `NewsState` / `NewsEntry`

- Wizard record and the 24-entry news feed (`pushNews` extensions on `NewsState`/`GameState`).
- `effectiveTitle()` / `effectiveIdeology()` resolve fallbacks when wizard data is missing.

