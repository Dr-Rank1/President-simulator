package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

/**
 * Oxiwyle-style world domination layer:
 * conquered territories, the three victory paths, military industry,
 * the new-game setup record, and the rolling news ticker feed.
 */

/** Fate the player chooses for a defeated nation. */
@Serializable
enum class TerritoryStatus(val displayName: String) {
    ANNEXED("Annexed"),
    PUPPET("Puppet State"),
    LIBERATED("Liberated"),
}

@Serializable
data class ConqueredTerritory(
    val countryId: String,
    val name: String,
    val flagEmoji: String,
    val status: TerritoryStatus,
    val year: Int,
    val month: Int,
)

@Serializable
data class TerritoryState(
    val conquered: List<ConqueredTerritory> = emptyList(),
) {
    val annexedCount: Int get() = conquered.count { it.status == TerritoryStatus.ANNEXED }
    val puppetCount: Int get() = conquered.count { it.status == TerritoryStatus.PUPPET }
    val liberatedCount: Int get() = conquered.count { it.status == TerritoryStatus.LIBERATED }
    /** Every nation under this government's control — the conquest score. */
    val controlledCount: Int get() = conquered.count { it.status != TerritoryStatus.LIBERATED }

    /** Monthly tribute extracted from puppet states. */
    val monthlyTributeIncome: Long
        get() = conquered
            .filter { it.status == TerritoryStatus.PUPPET }
            .sumOf { 1_200_000_000L }

    /** Annexed lands resist occupation, adding monthly instability. */
    val annexationInstabilityPressure: Float
        get() = annexedCount * 0.35f
}

/** The three Oxiwyle-style victory paths. */
@Serializable
enum class VictoryPath(val displayName: String, val goalLabel: String) {
    MILITARY_DOMINANCE("Military Dominance", "Bring 4 rival nations under your control"),
    RELIGIOUS_DOMINANCE("Religious Dominance", "Spread the state faith until it sweeps the world"),
    IDEOLOGICAL_DOMINANCE("Ideological Dominance", "Export your ideology until the world adopts it"),
}

/** Thresholds shared by engine and UI. */
object VictoryThresholds {
    const val NATIONS_TO_CONTROL = 4
    const val INFLUENCE_TO_WIN = 85f
}

@Serializable
data class VictoryPathState(
    val chosenPath: VictoryPath? = null,
    /** 0–100 spread of the state faith across the world. */
    val religiousInfluence: Float = 0f,
    /** 0–100 spread of the national ideology across the world. */
    val ideologicalInfluence: Float = 0f,
) {
    val militaryProgress: Float
        get() = 0f // computed against live territory counts; see VictoryEngine.progressFor

    fun progressFraction(controlled: Int): Float = when (chosenPath) {
        VictoryPath.MILITARY_DOMINANCE ->
            (controlled.toFloat() / VictoryThresholds.NATIONS_TO_CONTROL).coerceIn(0f, 1f)
        VictoryPath.RELIGIOUS_DOMINANCE ->
            (religiousInfluence / VictoryThresholds.INFLUENCE_TO_WIN).coerceIn(0f, 1f)
        VictoryPath.IDEOLOGICAL_DOMINANCE ->
            (ideologicalInfluence / VictoryThresholds.INFLUENCE_TO_WIN).coerceIn(0f, 1f)
        null -> 0f
    }
}

/**
 * Military industry: dedicated facilities that produce hardware every month,
 * mirroring Oxiwyle's arsenals, airfields, and shipyards.
 */
@Serializable
data class MilitaryIndustryState(
    val arsenals: Int = 0,
    val airfields: Int = 0,
    val shipyards: Int = 0,
    val lastTanksProduced: Int = 0,
    val lastJetsProduced: Int = 0,
    val lastShipsProduced: Int = 0,
) {
    val tanksPerMonth: Int get() = arsenals * 2
    val jetsPerMonth: Int get() = airfields * 1
    val shipsPerMonth: Int get() = shipyards * 1

    val totalUpkeep: Long
        get() = arsenals * 300_000_000L + airfields * 400_000_000L + shipyards * 500_000_000L

    val facilityCount: Int get() = arsenals + airfields + shipyards
}

@Serializable
enum class MilitaryFacilityType(val displayName: String, val produces: String, val unitCost: Long) {
    ARSENAL("Arsenal", "2 tanks / month", 8_000_000_000L),
    AIRFIELD("Airfield", "1 jet / month", 10_000_000_000L),
    SHIPYARD("Shipyard", "1 ship / month", 12_000_000_000L),
}

/**
 * Record of the new-game wizard: leader title, chosen ideology and religion.
 */
@Serializable
data class SetupState(
    val leaderTitle: String = "",
    val ideologyId: String = "",
    val religionId: String = "",
    val difficultyId: String = "standard",
) {
    val hasCustomIdeology: Boolean get() = ideologyId.isNotBlank()
    val hasCustomReligion: Boolean get() = religionId.isNotBlank()
}

/** One line of the rolling world news ticker. */
@Serializable
data class NewsEntry(
    val id: String,
    val year: Int,
    val month: Int,
    val headline: String,
    val tag: String = "WORLD",
)

@Serializable
data class NewsState(
    val entries: List<NewsEntry> = emptyList(),
) {
    companion object {
        const val MAX_ENTRIES = 24
    }
}

/** Convenience mutators used by engines and the view model. */
fun NewsState.push(year: Int, month: Int, headline: String, tag: String = "WORLD"): NewsState =
    copy(
        entries = (
            listOf(
                NewsEntry(
                    id = "${year}_${month}_${headline.hashCode()}_${System.nanoTime()}",
                    year = year,
                    month = month,
                    headline = headline,
                    tag = tag,
                ),
            ) + entries
            ).take(NewsState.MAX_ENTRIES),
    )

fun GameState.pushNews(headline: String, tag: String = "WORLD"): GameState =
    copy(news = news.push(year, month, headline, tag))

/** Leader title the HUD and dialogs should display. */
fun GameState.effectiveTitle(): String =
    setup.leaderTitle.ifBlank { legal.governmentSystem.executiveTitle }

/** Ideology in force — wizard choice overrides the nation default. */
fun GameState.effectiveIdeology(): Ideology =
    if (setup.hasCustomIdeology) {
        runCatching { Ideology.valueOf(setup.ideologyId) }.getOrDefault(legal.ideology)
    } else {
        legal.ideology
    }
