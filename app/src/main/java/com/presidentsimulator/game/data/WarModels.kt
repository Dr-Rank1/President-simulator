package com.presidentsimulator.game.data

/**
 * Snapshot shown when a war ends in victory or defeat.
 */
data class WarOutcome(
    val victory: Boolean,
    val targetCountryId: String,
    val targetName: String,
    val monthsActive: Int,
    val playerCasualties: Long,
    val enemyCasualties: Long,
    val budgetDelta: Long,
    val approvalDelta: Float,
    val finalProgress: Float,
    val warGoalLabel: String = "",
    val settlementNote: String = "",
    /** True when the defeated nation can be annexed, puppeted, or liberated. */
    val conquestAvailable: Boolean = false,
)
