package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

@Serializable
enum class ScenarioDifficulty(val displayName: String) {
    STANDARD("Standard"),
    HARD("Hard"),
    NIGHTMARE("Nightmare"),
}

@Serializable
data class ScenarioPack(
    val id: String,
    val title: String,
    val tagline: String,
    val difficulty: ScenarioDifficulty,
    val recommendedNationId: String? = null,
    val objectives: List<String> = emptyList(),
)

@Serializable
data class ScenarioState(
    val scenarioId: String = "standard",
    val title: String = "Standard Mandate",
    val challengeSeed: Int = 0,
    val victoryYearOverride: Int? = null,
    val notes: List<String> = emptyList(),
    val challengeId: String = "standard",
    val scoreMultiplier: Float = 1f,
)

data class CampaignChallenge(
    val id: String,
    val title: String,
    val description: String,
    val scoreMultiplier: Float,
)
