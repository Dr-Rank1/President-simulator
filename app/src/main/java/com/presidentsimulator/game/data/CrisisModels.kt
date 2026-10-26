package com.presidentsimulator.game.data

import com.presidentsimulator.game.R
import kotlinx.serialization.Serializable

@Serializable
enum class CrisisType(
    val displayName: String,
    val defaultTitle: String,
    val backdropDrawable: Int,
) {
    STRIKE(
        displayName = "Worker Strike",
        defaultTitle = "General Industrial Strike Declared",
        backdropDrawable = R.drawable.metkombinat,
    ),
    DEMONSTRATION(
        displayName = "Public Demonstration",
        defaultTitle = "Mass Rallies Outside Presidential Palace",
        backdropDrawable = R.drawable.fonmanifestation,
    ),
    MUTINY(
        displayName = "Military Mutiny",
        defaultTitle = "Garrison Disobedience in Border District",
        backdropDrawable = R.drawable.fongenshtab,
    ),
    EPIDEMIC(
        displayName = "Epidemic Outbreak",
        defaultTitle = "Virulent Contagion Sweeping Urban Hubs",
        backdropDrawable = R.drawable.fonepidemic,
    ),
    DISASTER(
        displayName = "Catastrophic Disaster",
        defaultTitle = "Critical Infrastructure Emergency",
        backdropDrawable = R.drawable.fondisaster,
    ),
}

@Serializable
data class CrisisOption(
    val id: String,
    val label: String,
    val costBudget: Long,
    val costAuthority: Int = 0,
    val description: String,
)

@Serializable
data class ActiveCrisis(
    val id: String,
    val type: CrisisType,
    val headline: String,
    val summary: String,
    val severityLevel: Int = 3, // 1 to 5
    val affectedSectorName: String = "National Economy",
    val ticksRemaining: Int = 4,
    val monthlyRevenuePenaltyMillion: Long = 1_500L,
    val monthlyApprovalPenalty: Float = 3.5f,
    val backdropDrawable: Int,
    val options: List<CrisisOption>,
)

@Serializable
data class CrisisState(
    val activeCrises: List<ActiveCrisis> = emptyList(),
    val hotlineRinging: Boolean = false,
    val totalCrisesResolved: Int = 0,
    val historicalDisasterDamageTotal: Long = 0L,
) {
    val hasUnresolvedCrises: Boolean get() = activeCrises.isNotEmpty()
}
