package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

@Serializable
enum class ScenarioDifficulty(val displayName: String) {
    EASY("Easy"),
    STANDARD("Standard"),
    HARD("Hard"),
    VERY_HARSH("Very Harsh"),
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
    /**
     * UTC epoch day of the Daily Seed Challenge this campaign belongs to;
     * 0 = ordinary campaign. Shared seed + nation make the run comparable.
     */
    val dailyChallengeEpochDay: Long = 0L,
)

data class CampaignChallenge(
    val id: String,
    val title: String,
    val description: String,
    val scoreMultiplier: Float,
)
