package com.presidentsimulator.game.data

import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Oxiwyle-style domination engine:
 * resolves conquest choices, drives religious/ideological world spread,
 * and awards the three path victories.
 */
object VictoryEngine {

    private val random = Random.Default

    /** Applies the player's fate for a defeated nation and returns the new state. */
    fun resolveConquest(state: GameState, targetCountryId: String, status: TerritoryStatus): GameState {
        val war = state.diplomacy.activeWar ?: return state
        if (war.targetCountryId != targetCountryId) return state
        val rival = state.diplomacy.rivalById(targetCountryId) ?: return state
        if (state.territory.conquered.any { it.countryId == targetCountryId }) return state

        val record = ConqueredTerritory(
            countryId = rival.id,
            name = rival.name,
            flagEmoji = rival.flagEmoji,
            status = status,
            year = state.year,
            month = state.month,
        )
        var next = state.copy(
            territory = state.territory.copy(conquered = state.territory.conquered + record),
        )
        next = when (status) {
            TerritoryStatus.ANNEXED -> {
                // Annexation: seize a slice of their economy and army, world opinion sours.
                next.copy(
                    vitals = next.vitals.copy(approval = (next.vitals.approval - 6f).coerceIn(0f, 100f)),
                    diplomacy = next.diplomacy.copy(
                        rivals = next.diplomacy.rivals.map {
                            if (it.id == targetCountryId) {
                                it.copy(relationshipScore = -100, hasTradeTreaty = false, hasNonAggressionPact = false)
                            } else {
                                it.copy(relationshipScore = (it.relationshipScore - 10).coerceIn(-100, 100))
                            }
                        },
                    ),
                ).pushNews("${next.effectiveTitle()} annexes ${rival.name} — world condemnation follows", tag = "WAR")
            }
            TerritoryStatus.PUPPET -> {
                // Puppet regime: tribute flows, the puppet is friendly but servile.
                next.copy(
                    diplomacy = next.diplomacy.copy(
                        rivals = next.diplomacy.rivals.map {
                            if (it.id == targetCountryId) {
                                it.copy(relationshipScore = 55, hasTradeTreaty = true, hasNonAggressionPact = true)
                            } else {
                                it.copy(relationshipScore = (it.relationshipScore - 4).coerceIn(-100, 100))
                            }
                        },
                    ),
                ).pushNews("${rival.name} becomes a puppet state under ${next.effectiveTitle()}", tag = "WAR")
            }
            TerritoryStatus.LIBERATED -> {
                // Liberation: the freed nation is grateful; world opinion improves.
                next.copy(
                    vitals = next.vitals.copy(approval = (next.vitals.approval + 8f).coerceIn(0f, 100f)),
                    diplomacy = next.diplomacy.copy(
                        rivals = next.diplomacy.rivals.map {
                            if (it.id == targetCountryId) {
                                it.copy(relationshipScore = 70, hasNonAggressionPact = true)
                            } else {
                                it.copy(relationshipScore = (it.relationshipScore + 6).coerceIn(-100, 100))
                            }
                        },
                    ),
                ).pushNews("${rival.name} liberated — celebrations worldwide", tag = "WAR")
            }
        }
        return next
    }

    /** Monthly spread of the state faith and the national ideology across the world. */
    fun processMonth(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state

        var next = state
        val religion = next.society.stateReligion
        if (religion != StateReligion.SECULAR) {
            // Faith spreads through culture, health, and puppet broadcasting.
            val pressure = next.society.cultureScore * 0.020f +
                next.society.healthLevel * 0.008f +
                next.territory.puppetCount * 0.9f +
                (if (religion == StateReligion.TRADITIONAL) 0.15f else 0.25f)
            next = next.copy(
                victoryPath = next.victoryPath.copy(
                    religiousInfluence = (next.victoryPath.religiousInfluence + pressure)
                        .coerceIn(0f, 100f),
                ),
            )
        }

        // Ideology spreads through prosperity, stability, and controlled nations.
        val ideologyPressure = when (next.effectiveIdeology()) {
            Ideology.DEMOCRACY -> next.vitals.approval * 0.012f + next.society.educationLevel * 0.008f
            Ideology.AUTOCRACY -> next.internalSecurity.instabilityScore * -0.004f + 0.35f +
                next.military.combatStrength.toFloat() * 0.0008f
            Ideology.COMMUNISM -> next.economy.factories * 0.045f + next.society.healthLevel * 0.006f
        } + (next.territory.controlledCount * 0.8f)
        next = next.copy(
            victoryPath = next.victoryPath.copy(
                ideologicalInfluence = (next.victoryPath.ideologicalInfluence + ideologyPressure)
                    .coerceIn(0f, 100f),
            ),
        )

        // Annexed lands breed resistance.
        if (next.territory.annexationInstabilityPressure > 0f) {
            next = next.copy(
                internalSecurity = next.internalSecurity.copy(
                    instabilityScore = (next.internalSecurity.instabilityScore +
                        next.territory.annexationInstabilityPressure).coerceIn(0f, 100f),
                ),
            )
        }

        // Rivals occasionally warm to the player's ideology when influence is high.
        if (next.victoryPath.ideologicalInfluence >= 55f) {
            val conqueredIds = next.territory.conquered.map { it.countryId }.toSet()
            val flipChance = 0.06f + (next.victoryPath.ideologicalInfluence - 55f) * 0.004f
            next = next.copy(
                diplomacy = next.diplomacy.copy(
                    rivals = next.diplomacy.rivals.map { rival ->
                        if (rival.id !in conqueredIds &&
                            rival.relationshipScore >= 0 &&
                            random.nextFloat() < flipChance
                        ) {
                            rival.copy(relationshipScore = (rival.relationshipScore + 5).coerceIn(-100, 100))
                        } else rival
                    },
                ),
            )
        }

        return checkVictories(next)
    }

    /** Awards the chosen victory once its threshold is met. */
    fun checkVictories(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state
        val path = state.victoryPath.chosenPath ?: return state
        val controlled = state.territory.controlledCount
        val progress = state.victoryPath.progressFraction(controlled)

        val won = when (path) {
            VictoryPath.MILITARY_DOMINANCE -> controlled >= VictoryThresholds.NATIONS_TO_CONTROL
            VictoryPath.RELIGIOUS_DOMINANCE -> state.victoryPath.religiousInfluence >= VictoryThresholds.INFLUENCE_TO_WIN
            VictoryPath.IDEOLOGICAL_DOMINANCE -> state.victoryPath.ideologicalInfluence >= VictoryThresholds.INFLUENCE_TO_WIN
        }
        if (!won) return state

        val reason = when (path) {
            VictoryPath.MILITARY_DOMINANCE ->
                "Victory: Military dominance — $controlled nations bow to ${state.effectiveTitle()} ${state.playerNation.flagEmoji}."
            VictoryPath.RELIGIOUS_DOMINANCE ->
                "Victory: Religious dominance — ${state.society.stateReligion.displayName} sweeps the world."
            VictoryPath.IDEOLOGICAL_DOMINANCE ->
                "Victory: Ideological dominance — ${state.effectiveIdeology().displayName} is the model of the age."
        }
        return state.copy(
            gameOver = GameOverState(isGameOver = true, isVictory = true, reason = reason),
        ).pushNews("HISTORIC: $reason", tag = "VICTORY")
    }

    /** Human-readable monthly delta notes for the news ticker. */
    fun spreadHeadline(state: GameState): String? {
        val path = state.victoryPath.chosenPath ?: return null
        val progress = (state.victoryPath.progressFraction(state.territory.controlledCount) * 100).toInt()
        return when (path) {
            VictoryPath.MILITARY_DOMINANCE ->
                "The world watches: $progress% toward total domination"
            VictoryPath.RELIGIOUS_DOMINANCE ->
                "Missionaries report ${state.victoryPath.religiousInfluence.roundToInt()}% of the world follows ${state.society.stateReligion.displayName}"
            VictoryPath.IDEOLOGICAL_DOMINANCE ->
                "Think tanks gauge ${state.effectiveIdeology().displayName} at ${state.victoryPath.ideologicalInfluence.roundToInt()}% global influence"
        }
    }

    /** Milestone news at 25/50/75% progress so the player feels momentum. */
    fun maybeEmitMilestone(state: GameState): GameState {
        val path = state.victoryPath.chosenPath ?: return state
        val progress = state.victoryPath.progressFraction(state.territory.controlledCount)
        val milestone = when {
            progress >= 0.75f -> 75
            progress >= 0.50f -> 50
            progress >= 0.25f -> 25
            else -> return state
        }
        return if (state.news.entries.none { it.tag == "DOMINION" && it.headline.contains("$milestone%") }) {
            state.pushNews(
                "$milestone% milestone reached on the ${path.displayName} path",
                tag = "DOMINION",
            )
        } else state
    }
}

/**
 * Military industry monthly production — hardware rolls off the lines
 * and Treasury pays the upkeep.
 */
object MilitaryIndustryEngine {

    fun processMonth(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state
        val industry = state.militaryIndustry
        if (industry.facilityCount == 0) return state

        val tanks = industry.tanksPerMonth
        val jets = industry.jetsPerMonth
        val ships = industry.shipsPerMonth
        var next = state.copy(
            military = state.military.copy(
                tanks = state.military.tanks + tanks,
                jets = state.military.jets + jets,
                ships = state.military.ships + ships,
            ),
            militaryIndustry = industry.copy(
                lastTanksProduced = tanks,
                lastJetsProduced = jets,
                lastShipsProduced = ships,
            ),
        )
        if (tanks + jets + ships > 0) {
            next = next.pushNews(
                "Military industry delivers: ${tanks} tanks, ${jets} jets, ${ships} ships",
                tag = "MILITARY",
            )
        }
        return next
    }
}
