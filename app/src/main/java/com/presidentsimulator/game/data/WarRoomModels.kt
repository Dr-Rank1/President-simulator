package com.presidentsimulator.game.data

import com.presidentsimulator.game.R
import kotlinx.serialization.Serializable

@Serializable
enum class MilitaryDoctrine(
    val title: String,
    val description: String,
    val attackBonus: Float,
    val defenseBonus: Float,
    val casualtyReduction: Float,
) {
    BLITZKRIEG(
        title = "Shock & Blitzkrieg",
        description = "Overwhelming mobile armor strikes. High offensive tempo with elevated material wear.",
        attackBonus = 0.35f,
        defenseBonus = -0.10f,
        casualtyReduction = -0.05f,
    ),
    IRON_DEFENSE(
        title = "Iron Bastion",
        description = "Fortified defensive lines, active air shield, and attrition warfare against invaders.",
        attackBonus = -0.10f,
        defenseBonus = 0.40f,
        casualtyReduction = 0.25f,
    ),
    COMBINED_ARMS(
        title = "Integrated Combined Arms",
        description = "Balanced synchronization of artillery, air superiority, armored divisions, and infantry.",
        attackBonus = 0.15f,
        defenseBonus = 0.15f,
        casualtyReduction = 0.15f,
    ),
}

@Serializable
data class StaffCommander(
    val id: String,
    val name: String,
    val roleTitle: String, // e.g. "Chief of General Staff", "Supreme Air Marshal", "Fleet Admiral"
    val rank: String, // e.g. "General of the Army", "Air Chief Marshal"
    val portraitDrawable: Int,
    val doctrine: MilitaryDoctrine,
    val experienceStars: Int = 3, // 1 to 5
    val loyaltyPercent: Int = 85,
    val monthlySalaryMillion: Long = 25L,
)

@Serializable
data class ArmyCasualtyLedger(
    val troopsFallen: Long = 0L,
    val tanksLost: Int = 0,
    val jetsLost: Int = 0,
    val shipsLost: Int = 0,
    val enemyTroopsNeutralized: Long = 0L,
    val enemyTanksDestroyed: Int = 0,
    val enemyJetsDowned: Int = 0,
    val majorBattlesFought: Int = 0,
    val totalVictories: Int = 0,
    val totalDefeats: Int = 0,
)

@Serializable
data class NuclearSiloState(
    val nuclearWarheads: Int = 24, // Stockpile
    val activeSilos: Int = 8,
    val defconLevel: Int = 5, // 5 (Peacetime) down to 1 (Nuclear War Imminent)
    val launchKeysInserted: Boolean = false,
    val twoPersonRuleConfirmed: Boolean = false,
    val deterrenceIndex: Int = 78, // Reduces chance of foreign invasions
    val maintenanceCostMonthlyMillion: Long = 180L,
)

@Serializable
data class WarRoomState(
    val selectedDoctrine: MilitaryDoctrine = MilitaryDoctrine.COMBINED_ARMS,
    val commanders: List<StaffCommander> = defaultCommanders(),
    val casualtyLedger: ArmyCasualtyLedger = ArmyCasualtyLedger(),
    val nuclearSilo: NuclearSiloState = NuclearSiloState(),
) {
    companion object {
        fun defaultCommanders(): List<StaffCommander> = listOf(
            StaffCommander(
                id = "army_chief",
                name = "General Marcus Vance",
                roleTitle = "Chief of the General Staff",
                rank = "General of the Army (4-Star)",
                portraitDrawable = R.drawable.groundtroops,
                doctrine = MilitaryDoctrine.COMBINED_ARMS,
                experienceStars = 4,
                loyaltyPercent = 90,
                monthlySalaryMillion = 30L,
            ),
            StaffCommander(
                id = "air_chief",
                name = "Marshal Victoria Sterling",
                roleTitle = "Supreme Air Force Commander",
                rank = "Air Chief Marshal (4-Star)",
                portraitDrawable = R.drawable.aviation,
                doctrine = MilitaryDoctrine.BLITZKRIEG,
                experienceStars = 5,
                loyaltyPercent = 88,
                monthlySalaryMillion = 28L,
            ),
            StaffCommander(
                id = "navy_chief",
                name = "Admiral Arthur Drake",
                roleTitle = "Chief of Naval Operations",
                rank = "Fleet Admiral (4-Star)",
                portraitDrawable = R.drawable.vms,
                doctrine = MilitaryDoctrine.IRON_DEFENSE,
                experienceStars = 4,
                loyaltyPercent = 92,
                monthlySalaryMillion = 26L,
            ),
        )
    }
}
