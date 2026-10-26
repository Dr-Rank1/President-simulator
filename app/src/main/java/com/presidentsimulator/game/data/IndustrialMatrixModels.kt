package com.presidentsimulator.game.data

import com.presidentsimulator.game.R
import kotlinx.serialization.Serializable

/**
 * 12 Industrial Sectors reverse-engineered directly from APK classes:
 * Chemical, Power (AES/GES/TES), Engineering, Ferrous Metallurgy,
 * Non-Ferrous Smelting, Food/Agri, Fuel/Gas, Glass, Light Industry,
 * Medical/Pharma, Defense Industrial Complex, Timber/Forestry.
 */
@Serializable
enum class IndustrialSectorType(
    val title: String,
    val iconDrawable: Int,
    val defaultWorkers: Long,
    val baseOutputMillion: Long,
) {
    CHEMICAL(
        title = "Chemical Industry",
        iconDrawable = R.drawable.chemicalindustry,
        defaultWorkers = 120_000L,
        baseOutputMillion = 3_200L,
    ),
    POWER(
        title = "Electrical Power Industry",
        iconDrawable = R.drawable.electricity,
        defaultWorkers = 180_000L,
        baseOutputMillion = 5_800L,
    ),
    ENGINEERING(
        title = "Mechanical Engineering",
        iconDrawable = R.drawable.engineering,
        defaultWorkers = 250_000L,
        baseOutputMillion = 6_400L,
    ),
    FERROUS_METALLURGY(
        title = "Ferrous Metallurgy",
        iconDrawable = R.drawable.ferrousmetallurgy,
        defaultWorkers = 150_000L,
        baseOutputMillion = 4_500L,
    ),
    NON_FERROUS_METALLURGY(
        title = "Non-Ferrous Smelting",
        iconDrawable = R.drawable.nonferrousmetallurgy,
        defaultWorkers = 110_000L,
        baseOutputMillion = 3_800L,
    ),
    FOOD_AGRI(
        title = "Food & Agriculture",
        iconDrawable = R.drawable.foodindestry,
        defaultWorkers = 320_000L,
        baseOutputMillion = 4_900L,
    ),
    FUEL_ENERGY(
        title = "Fuel & Hydrocarbons",
        iconDrawable = R.drawable.fuelindustry,
        defaultWorkers = 200_000L,
        baseOutputMillion = 7_200L,
    ),
    GLASS_CERAMICS(
        title = "Glass & Technical Ceramics",
        iconDrawable = R.drawable.glassindustry,
        defaultWorkers = 80_000L,
        baseOutputMillion = 2_100L,
    ),
    LIGHT_INDUSTRY(
        title = "Light Industry & Textiles",
        iconDrawable = R.drawable.lightindustry,
        defaultWorkers = 210_000L,
        baseOutputMillion = 3_400L,
    ),
    MEDICAL_PHARMA(
        title = "Pharmaceuticals & Healthcare",
        iconDrawable = R.drawable.medicalindustry,
        defaultWorkers = 95_000L,
        baseOutputMillion = 4_100L,
    ),
    DEFENSE_COMPLEX(
        title = "Defense Industrial Complex",
        iconDrawable = R.drawable.militaryindustry,
        defaultWorkers = 190_000L,
        baseOutputMillion = 6_000L,
    ),
    TIMBER_FORESTRY(
        title = "Forestry & Wood Processing",
        iconDrawable = R.drawable.forestindustry,
        defaultWorkers = 130_000L,
        baseOutputMillion = 2_700L,
    ),
}

@Serializable
data class SubPlant(
    val name: String,
    val imageDrawable: Int,
    val count: Int,
    val outputUnitBonus: Long,
    val buildCostMillion: Long,
)

@Serializable
data class IndustrialSectorData(
    val type: IndustrialSectorType,
    val level: Int = 1,
    val facilitiesCount: Int = 10,
    val workers: Long = 100_000L,
    val monthlyOutputMillion: Long = 2_500L,
    val monthlyUpkeepMillion: Long = 600L,
    val efficiencyMultiplier: Float = 1.0f,
    val isUnderStrike: Boolean = false,
    val subPlants: List<SubPlant> = emptyList(),
) {
    val netRevenue: Long
        get() = if (isUnderStrike) {
            (monthlyOutputMillion * 1_000_000L * 0.4f * efficiencyMultiplier).toLong() - (monthlyUpkeepMillion * 1_000_000L)
        } else {
            (monthlyOutputMillion * 1_000_000L * efficiencyMultiplier).toLong() - (monthlyUpkeepMillion * 1_000_000L)
        }
}

@Serializable
data class IndustrialMatrixState(
    val sectors: Map<IndustrialSectorType, IndustrialSectorData> = initialSectors(),
) {
    val totalWorkforce: Long get() = sectors.values.sumOf { it.workers }
    val totalMonthlyRevenue: Long get() = sectors.values.sumOf { it.netRevenue }
    val activeStrikesCount: Int get() = sectors.values.count { it.isUnderStrike }

    companion object {
        fun initialSectors(): Map<IndustrialSectorType, IndustrialSectorData> {
            return IndustrialSectorType.entries.associateWith { type ->
                val subPlants = when (type) {
                    IndustrialSectorType.POWER -> listOf(
                        SubPlant("Nuclear AES", R.drawable.aes, 2, 800L, 12_000L),
                        SubPlant("Hydro GES", R.drawable.hydropowerplant, 4, 450L, 6_000L),
                        SubPlant("Thermal TES", R.drawable.thermalpowerplant, 6, 300L, 3_500L),
                        SubPlant("Solar SES", R.drawable.solarpowerplant, 3, 180L, 2_200L),
                        SubPlant("Wind VES", R.drawable.windpowerstation, 5, 120L, 1_800L),
                    )
                    IndustrialSectorType.CHEMICAL -> listOf(
                        SubPlant("Plastics Plant", R.drawable.plasticsplant, 3, 250L, 2_500L),
                        SubPlant("Nitrogen Fertilizer", R.drawable.nitrogenplant, 4, 310L, 3_000L),
                        SubPlant("Soda & Alkaline", R.drawable.sodaplant, 2, 190L, 1_900L),
                        SubPlant("Phosphate Synthesis", R.drawable.phosphateplant, 3, 220L, 2_100L),
                    )
                    IndustrialSectorType.ENGINEERING -> listOf(
                        SubPlant("Machine Tool Works", R.drawable.machinetoolplant, 5, 400L, 4_200L),
                        SubPlant("Tractor Factory", R.drawable.tractorplant, 3, 280L, 3_100L),
                        SubPlant("Automobile Complex", R.drawable.automobilefactory, 4, 520L, 5_500L),
                        SubPlant("Aviation Plant", R.drawable.aircraftfactory, 1, 950L, 15_000L),
                        SubPlant("Locomotive Works", R.drawable.locomotiveplant, 2, 340L, 3_800L),
                    )
                    IndustrialSectorType.FERROUS_METALLURGY -> listOf(
                        SubPlant("Integrated Steel Works", R.drawable.metkombinat, 4, 600L, 7_000L),
                        SubPlant("Manganese Smelter", R.drawable.manganeseplant, 3, 290L, 3_200L),
                    )
                    IndustrialSectorType.NON_FERROUS_METALLURGY -> listOf(
                        SubPlant("Aluminum Smelter", R.drawable.aluminumplant, 3, 420L, 4_800L),
                        SubPlant("Copper Works", R.drawable.coppersmeltingplant, 4, 380L, 4_200L),
                        SubPlant("Nickel Smelter", R.drawable.nickelplant, 2, 310L, 3_600L),
                        SubPlant("Zinc Electrolysis", R.drawable.zincplant, 3, 260L, 2_900L),
                    )
                    IndustrialSectorType.FOOD_AGRI -> listOf(
                        SubPlant("Mechanized Bakery", R.drawable.mechanizedbakery, 6, 210L, 1_500L),
                        SubPlant("Dairy Processing", R.drawable.dairy, 5, 230L, 1_800L),
                        SubPlant("Creamery", R.drawable.creamery, 3, 160L, 1_400L),
                        SubPlant("Sugar Refinery", R.drawable.sugarfactory, 4, 270L, 2_400L),
                        SubPlant("Canning Plant", R.drawable.canningfactory, 4, 190L, 1_700L),
                        SubPlant("Winery & Vineyard", R.drawable.winery, 2, 180L, 1_600L),
                    )
                    IndustrialSectorType.FUEL_ENERGY -> listOf(
                        SubPlant("Oil Extraction Rig", R.drawable.neftplant, 5, 650L, 8_000L),
                        SubPlant("Offshore Oil Platform", R.drawable.neftplatforma, 2, 980L, 14_000L),
                        SubPlant("Gas Processing Plant", R.drawable.gasplant, 4, 520L, 6_500L),
                        SubPlant("Offshore Gas Platform", R.drawable.gasplatform, 2, 750L, 11_000L),
                        SubPlant("Coal Colliery", R.drawable.ugolplant, 5, 330L, 3_200L),
                    )
                    IndustrialSectorType.GLASS_CERAMICS -> listOf(
                        SubPlant("Industrial Glassworks", R.drawable.glasswarefactory, 4, 240L, 2_100L),
                        SubPlant("Porcelain Factory", R.drawable.porcelainfactory, 3, 180L, 1_800L),
                        SubPlant("Refractory Materials", R.drawable.refractoryplant, 3, 210L, 2_200L),
                    )
                    IndustrialSectorType.LIGHT_INDUSTRY -> listOf(
                        SubPlant("Cotton Fabrics Mill", R.drawable.cottonfabrics, 5, 260L, 2_400L),
                        SubPlant("Linen Processing", R.drawable.plantlinen, 4, 210L, 1_900L),
                        SubPlant("Carpet & Rug Complex", R.drawable.plantrugs, 3, 180L, 1_600L),
                        SubPlant("Wool Weaving Plant", R.drawable.woolenfabrics, 4, 230L, 2_100L),
                    )
                    IndustrialSectorType.MEDICAL_PHARMA -> listOf(
                        SubPlant("Medicine Synthesis", R.drawable.plantmedicines, 4, 450L, 5_000L),
                        SubPlant("Vitamin Formulation", R.drawable.vitaminplant, 3, 280L, 3_100L),
                        SubPlant("Prosthetics Factory", R.drawable.prostheticplant, 2, 220L, 2_500L),
                        SubPlant("Medical Hardware", R.drawable.factorymedicalequipment, 3, 390L, 4_400L),
                    )
                    IndustrialSectorType.DEFENSE_COMPLEX -> listOf(
                        SubPlant("Fighter Assembly", R.drawable.productionfighters, 2, 850L, 12_000L),
                        SubPlant("Radar & Air Defense", R.drawable.radarsystems, 3, 620L, 8_500L),
                        SubPlant("Naval Dockyard", R.drawable.navalstation, 1, 1_200L, 20_000L),
                    )
                    IndustrialSectorType.TIMBER_FORESTRY -> listOf(
                        SubPlant("Logging Operations", R.drawable.lumbering, 6, 260L, 1_800L),
                        SubPlant("Paper Mill", R.drawable.papermill, 4, 310L, 2_800L),
                        SubPlant("Cellulose Pulp Mill", R.drawable.pulpmill, 3, 270L, 2_600L),
                        SubPlant("Furniture Factory", R.drawable.furniturefactory, 4, 340L, 3_200L),
                    )
                }

                IndustrialSectorData(
                    type = type,
                    level = 1,
                    facilitiesCount = 5 + subPlants.size,
                    workers = type.defaultWorkers,
                    monthlyOutputMillion = type.baseOutputMillion,
                    monthlyUpkeepMillion = (type.baseOutputMillion * 0.22f).toLong(),
                    efficiencyMultiplier = 1.0f,
                    isUnderStrike = false,
                    subPlants = subPlants,
                )
            }
        }
    }
}
