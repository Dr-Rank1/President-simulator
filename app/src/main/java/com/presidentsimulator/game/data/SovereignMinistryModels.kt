package com.presidentsimulator.game.data

import com.presidentsimulator.game.R
import kotlinx.serialization.Serializable

@Serializable
data class PoliceDepartmentState(
    val hooliganismRatePercent: Float = 24.5f,
    val violentCrimeRatePercent: Float = 12.0f,
    val corruptionIndexPercent: Float = 35.0f,
    val patrolCars: Int = 1_200,
    val stunBatons: Int = 5_000,
    val servicePistols: Int = 4_500,
    val swatArmoredVehicles: Int = 180,
    val prosecutorsOffices: Int = 45,
    val highSecurityPrisons: Int = 22,
    val officerHeadcount: Long = 45_000L,
    val monthlyBudgetMillion: Long = 420L,
) {
    val overallCrimeSafetyScore: Int
        get() = (100f - (hooliganismRatePercent * 0.4f + violentCrimeRatePercent * 1.5f + corruptionIndexPercent * 0.3f))
            .coerceIn(0f, 100f)
            .toInt()
}

@Serializable
data class OlympicSportsState(
    val footballStadiums: Int = 12,
    val iceArenas: Int = 8,
    val basketballArenas: Int = 10,
    val velodromes: Int = 4,
    val racecourses: Int = 3,
    val olympicPools: Int = 6,
    val nationalAthletesCount: Int = 1_400,
    val internationalMedalsWon: Int = 28,
    val publicHappinessBonus: Float = 4.5f,
    val publicHealthBonus: Float = 3.8f,
) {
    val totalSportsFacilities: Int
        get() = footballStadiums + iceArenas + basketballArenas + velodromes + racecourses + olympicPools
}

@Serializable
data class EcologyState(
    val airQualityIndex: Int = 65, // 0-50 Good, 51-100 Moderate, 101+ Unhealthy
    val waterPurificationPlants: Int = 35,
    val wasteRecyclingCenters: Int = 28,
    val protectedNatureReserves: Int = 14,
    val renewableEnergySharePercent: Float = 28.0f,
    val publicHealthIndex: Float = 76.5f,
)

@Serializable
data class SovereignMinistriesState(
    val police: PoliceDepartmentState = PoliceDepartmentState(),
    val sports: OlympicSportsState = OlympicSportsState(),
    val ecology: EcologyState = EcologyState(),
)
