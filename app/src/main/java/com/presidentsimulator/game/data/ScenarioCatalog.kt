package com.presidentsimulator.game.data

import kotlin.random.Random

object ScenarioCatalog {
    val CHALLENGES = listOf(
        CampaignChallenge("standard", "EASY - Classic", "Balanced treasury and calm borders. Recommended for new leaders.", 1f),
        CampaignChallenge("austerity", "NORMAL - Austerity", "Constrained national budget with reduced treasury and public unrest.", 1.2f),
        CampaignChallenge("hostile_press", "HARD - Hostile Press", "Intense investigative media scrutiny and low government credibility.", 1.2f),
        CampaignChallenge("snap_election", "EXTREME - Crisis", "Emergency election in 12 months with strong opposition momentum.", 1.25f),
    )

    val ALL = listOf(
        ScenarioPack(
            id = "peaceful_opening",
            title = "Peaceful Opening",
            tagline = "A stronger treasury, steady public support, and calm borders.",
            difficulty = ScenarioDifficulty.EASY,
            objectives = listOf("Keep the monthly budget in surplus", "Maintain public approval above 55%", "Keep food supply stable"),
        ),
        ScenarioPack(
            id = "standard",
            title = "Standard Mandate",
            tagline = "Balanced start — write your own legacy.",
            difficulty = ScenarioDifficulty.STANDARD,
            objectives = listOf("Build a stable mandate", "Win the next election", "Leave a lasting national legacy"),
        ),
        ScenarioPack(
            id = "powder_keg",
            title = "Powder Keg",
            tagline = "Hostile neighbors, thin treasury, election in 18 months.",
            difficulty = ScenarioDifficulty.HARD,
            objectives = listOf("Restore the treasury", "Reduce tensions with neighboring states", "Survive the early election"),
        ),
        ScenarioPack(
            id = "empty_granaries",
            title = "Empty Granaries",
            tagline = "Food crisis on day one — feed the nation or fall.",
            difficulty = ScenarioDifficulty.HARD,
            objectives = listOf("End the food shortage", "Restore approval", "Build a resilient food supply"),
        ),
        ScenarioPack(
            id = "palace_intrigue",
            title = "Palace Intrigue",
            tagline = "Corrupt cabinet, hostile press, restless officers.",
            difficulty = ScenarioDifficulty.HARD,
            objectives = listOf("Restore cabinet cohesion", "Rebuild public trust", "Keep coup risk under control"),
        ),
        ScenarioPack(
            id = "iron_curtain",
            title = "Iron Curtain",
            tagline = "Autocratic grip, embargoed trade, UN spotlight.",
            difficulty = ScenarioDifficulty.VERY_HARSH,
            recommendedNationId = "kryos",
            objectives = listOf("Break the diplomatic isolation", "Keep the regime stable", "Reach the national victory year"),
        ),
        ScenarioPack(
            id = "reform_or_die",
            title = "Reform or Die",
            tagline = "Minority government, surging opposition, ticking clock.",
            difficulty = ScenarioDifficulty.VERY_HARSH,
            objectives = listOf("Pass legislation with a minority government", "Contain opposition momentum", "Win the early election"),
        ),
    )

    fun byId(id: String): ScenarioPack = ALL.find { it.id == id } ?: ALL.first()

    fun apply(
        state: GameState,
        scenarioId: String,
        seed: Int = Random.Default.nextInt(),
        challengeId: String = "standard",
    ): GameState {
        val pack = byId(scenarioId)
        val rng = Random(seed)
        var next = state.copy(
            scenario = ScenarioState(
                scenarioId = pack.id,
                title = pack.title,
                challengeSeed = seed,
                notes = listOf(pack.tagline),
            ),
        )

        next = when (pack.id) {
            "peaceful_opening" -> next.copy(
                vitals = next.vitals.copy(
                    budget = (next.vitals.budget * 1.35).toLong(),
                    approval = (next.vitals.approval + 10f).coerceAtMost(85f),
                ),
                production = next.production.copy(food = next.production.food + 2_500L),
                diplomacy = next.diplomacy.copy(
                    rivals = next.diplomacy.rivals.map { rival ->
                        rival.copy(relationshipScore = (rival.relationshipScore + 12).coerceAtMost(100))
                    },
                ),
                internalSecurity = next.internalSecurity.copy(
                    instabilityScore = (next.internalSecurity.instabilityScore - 12f).coerceAtLeast(0f),
                    coupRisk = (next.internalSecurity.coupRisk - 12f).coerceAtLeast(0f),
                ),
                scenario = next.scenario.copy(notes = next.scenario.notes + "Peaceful opening · extra reserves and stronger public support"),
            )
            "powder_keg" -> next.copy(
                vitals = next.vitals.copy(
                    budget = (next.vitals.budget * 0.55).toLong(),
                    approval = (next.vitals.approval - 8f).coerceIn(20f, 100f),
                ),
                nextElectionYear = next.year + 1,
                diplomacy = next.diplomacy.copy(
                    rivals = next.diplomacy.rivals.map { r ->
                        r.copy(relationshipScore = (r.relationshipScore - 25).coerceIn(-100, 100))
                    },
                ),
                military = next.military.copy(defcon = 3),
                scenario = next.scenario.copy(
                    victoryYearOverride = next.year + 12,
                    notes = next.scenario.notes + "Election accelerated · rivals hostile",
                ),
            )
            "empty_granaries" -> next.copy(
                production = next.production.copy(
                    food = 400L,
                    foodShortage = true,
                ),
                economy = next.economy.copy(farms = (next.economy.farms * 0.6).toInt().coerceAtLeast(8)),
                vitals = next.vitals.copy(approval = (next.vitals.approval - 10f).coerceIn(15f, 100f)),
                disaster = next.disaster.copy(readiness = 28f),
                scenario = next.scenario.copy(notes = next.scenario.notes + "Food stocks critical"),
            )
            "palace_intrigue" -> {
                val cabinet = next.cabinet.copy(
                    ministers = next.cabinet.ministers.map { m ->
                        if (rng.nextFloat() < 0.45f) {
                            m.copy(
                                traits = (m.traits + MinisterTrait.CORRUPT).distinct(),
                                scandalHeat = rng.nextInt(35, 60).toFloat(),
                                loyalty = (m.loyalty - 15f).coerceAtLeast(20f),
                            )
                        } else {
                            m
                        }
                    },
                    cohesion = 32f,
                )
                next.copy(
                    cabinet = cabinet,
                    press = next.press.copy(
                        mediaSentiment = 28f,
                        credibility = 35f,
                        leakRisk = 40f,
                    ),
                    internalSecurity = next.internalSecurity.copy(
                        coupRisk = 48f,
                        instabilityScore = 42f,
                    ),
                    scenario = next.scenario.copy(notes = next.scenario.notes + "Cabinet compromised"),
                )
            }
            "iron_curtain" -> next.copy(
                legal = next.legal.copy(ideology = Ideology.AUTOCRACY),
                diplomacy = next.diplomacy.copy(
                    rivals = next.diplomacy.rivals.map { r ->
                        r.copy(
                            relationshipScore = (r.relationshipScore - 35).coerceIn(-100, 40),
                            hasEmbargo = rng.nextFloat() < 0.4f,
                        )
                    },
                ),
                press = next.press.copy(pressFreedom = 22f, mediaSentiment = 40f, credibility = 30f),
                opposition = OppositionEngine.seedInitial(Ideology.AUTOCRACY, rng),
                scenario = next.scenario.copy(
                    victoryYearOverride = next.year + 16,
                    notes = next.scenario.notes + "Isolated autocracy",
                ),
            )
            "reform_or_die" -> {
                val base = if (next.opposition.parties.isEmpty()) {
                    OppositionEngine.seedInitial(next.legal.ideology, rng)
                } else {
                    next.opposition
                }
                val ruling = base.rulingParty ?: return next
                val main = base.mainOpposition ?: return next
                val others = base.parties.filter { !it.isRuling && it.id != main.id }
                val parties = buildList {
                    add(ruling.copy(seats = 44, popularity = 36f))
                    add(main.copy(seats = 34, popularity = 46f, hostility = 72f))
                    others.forEachIndexed { i, p ->
                        add(p.copy(seats = if (i == 0) 14 else 8, hostility = 55f))
                    }
                }
                next.copy(
                    opposition = base.copy(parties = parties, noConfidenceHeat = 35f),
                    nextElectionYear = next.year + 2,
                    demographics = next.demographics.copy(oppositionMomentum = 18f),
                    vitals = next.vitals.copy(approval = 42f),
                    scenario = next.scenario.copy(
                        notes = next.scenario.notes + "Minority government · early election",
                    ),
                )
            }
            else -> next
        }

        val challenge = CHALLENGES.find { it.id == challengeId } ?: CHALLENGES.first()
        next = when (challenge.id) {
            "austerity" -> next.copy(
                vitals = next.vitals.copy(
                    budget = (next.vitals.budget * 0.68f).toLong(),
                    approval = (next.vitals.approval - 5f).coerceAtLeast(10f),
                ),
                scenario = next.scenario.copy(notes = next.scenario.notes + "Austerity challenge active"),
            )
            "hostile_press" -> next.copy(
                press = next.press.copy(
                    mediaSentiment = (next.press.mediaSentiment - 18f).coerceAtLeast(5f),
                    credibility = (next.press.credibility - 15f).coerceAtLeast(5f),
                    leakRisk = (next.press.leakRisk + 10f).coerceAtMost(100f),
                ),
                scenario = next.scenario.copy(notes = next.scenario.notes + "Hostile press challenge active"),
            )
            "snap_election" -> next.copy(
                nextElectionYear = next.year + 1,
                vitals = next.vitals.copy(approval = (next.vitals.approval - 4f).coerceAtLeast(10f)),
                scenario = next.scenario.copy(notes = next.scenario.notes + "Snap election challenge active"),
            )
            else -> next
        }
        return next.copy(
            scenario = next.scenario.copy(challengeId = challenge.id, scoreMultiplier = challenge.scoreMultiplier),
            legacy = next.legacy.copy(
                lastLegacyNote = "Scenario: ${pack.title}",
                entries = listOf(
                    LegacyEntry(
                        id = "scenario_$seed",
                        year = next.year,
                        month = next.month,
                        title = "Mandate begins: ${pack.title}",
                        detail = pack.tagline,
                        pillar = LegacyPillar.MANDATE,
                        tone = LegacyTone.TURNING_POINT,
                        scoreDelta = 0,
                    ),
                ),
            ),
        )
    }
}
