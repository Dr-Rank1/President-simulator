package com.presidentsimulator.game.ui.components

import com.presidentsimulator.game.R

/**
 * Authentic game assets extracted directly via REA from the original President Simulator APK.
 * Loaded at runtime via Coil using local drawable resource IDs.
 */
object NssCardImages {
    val SERVICES: Any = R.drawable.clinic
    val INDUSTRY: Any = R.drawable.automobilefactory
    val MANUFACTURING: Any = R.drawable.aircraftfactory
    val TECHNOLOGY: Any = R.drawable.vuz
    val AGRICULTURE: Any = R.drawable.farm
    val ENERGY: Any = R.drawable.solarpowerplant
    val DEFENSE_IND: Any = R.drawable.radarsystems
    val NUCLEAR_PLANT: Any = R.drawable.aes
    val COAL_MINE: Any = R.drawable.ugolplant
    val SALT_MINE: Any = R.drawable.saltmining
    val HIGHRISE_HOUSING: Any = R.drawable.highrisebuilding
    val CLINIC: Any = R.drawable.clinic
    val UNIVERSITY: Any = R.drawable.vuz

    val INFANTRY: Any = R.drawable.soldierstraine
    val ARMORED: Any = R.drawable.pic_tank
    val ARTILLERY: Any = R.drawable.artillery
    val SPECIAL_OPS: Any = R.drawable.sso
    val DESTROYER: Any = R.drawable.armydestroyer
    val FRIGATE: Any = R.drawable.armycruiser
    val SUBMARINE: Any = R.drawable.armyapl
    val CARRIER: Any = R.drawable.armyaircraftcarrier
    val FIGHTER: Any = R.drawable.armyfighter
    val BOMBER: Any = R.drawable.armybomber
    val DRONE: Any = R.drawable.bpla

    val BANNER_ECONOMY: Any = R.drawable.worldtradelogo
    val BANNER_DEFENSE: Any = R.drawable.icon_swords
    val BANNER_FOREIGN: Any = R.drawable.unitednations
    val MAP: Any = R.drawable.world_map_vector
    val PARLIAMENT: Any = R.drawable.sud

    val LEADER_1: Any = R.drawable.admob
    val LEADER_2: Any = R.drawable.admob
    val LEADER_3: Any = R.drawable.admob
    val LEADER_4: Any = R.drawable.admob
    val LEADER_5: Any = R.drawable.admob
    val LEADER_6: Any = R.drawable.admob
    val LEADER_7: Any = R.drawable.admob
    val LEADER_8: Any = R.drawable.admob
    val LOCKED_BG: Any = R.drawable.back_start_dialog

    val BANNER_DOMESTIC: Any = R.drawable.highrisebuilding
    val BANNER_INTELLIGENCE: Any = R.drawable.prokuratura
    val BANNER_SCIENCE: Any = R.drawable.ic_science
    val BANNER_COMMAND: Any = R.drawable.icon_swords

    private val nationCardRotation = listOf(BANNER_FOREIGN, MAP, PARLIAMENT, BANNER_DEFENSE, DESTROYER, BANNER_ECONOMY)

    fun nationCardImage(index: Int): Any = nationCardRotation[index.mod(nationCardRotation.size)]

    fun eventImage(eventId: String): Any = when {
        eventId.contains("strike", ignoreCase = true) -> INDUSTRY
        eventId.contains("boom", ignoreCase = true) -> ENERGY
        eventId.contains("crash", ignoreCase = true) || eventId.contains("market", ignoreCase = true) -> BANNER_ECONOMY
        eventId.contains("epidemic", ignoreCase = true) -> SERVICES
        eventId.contains("border", ignoreCase = true) || eventId.contains("war", ignoreCase = true) -> INFANTRY
        else -> PARLIAMENT
    }

    fun techCategoryImage(category: com.presidentsimulator.game.data.TechCategory): Any = when (category) {
        com.presidentsimulator.game.data.TechCategory.ECONOMY -> TECHNOLOGY
        com.presidentsimulator.game.data.TechCategory.MILITARY -> DEFENSE_IND
        com.presidentsimulator.game.data.TechCategory.SOCIETY -> SERVICES
    }

    fun lawCategoryImage(category: com.presidentsimulator.game.data.LawCategory): Any = when (category) {
        com.presidentsimulator.game.data.LawCategory.MILITARY -> BANNER_DEFENSE
        com.presidentsimulator.game.data.LawCategory.ECONOMIC -> BANNER_ECONOMY
        com.presidentsimulator.game.data.LawCategory.SOCIAL -> PARLIAMENT
    }
}
