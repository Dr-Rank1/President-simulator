package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.ScenarioCatalog

/**
 * Builds the same seeded pipeline [GameViewModel] uses at runtime: one [kotlin.random.Random]
 * shared by every random-consuming engine, all reconstructed from the campaign seed so runs
 * replay exactly.
 */
internal fun seededTestPipeline(
    seed: Long,
    onStage: (String) -> Unit = {},
): MonthlySimulationPipeline {
    val random = kotlin.random.Random(seed)
    return MonthlySimulationPipeline(
        random = random,
        diplomacy = DiplomacyViewModel(random),
        productionLaw = ProductionLawViewModel(),
        analytics = AnalyticsSaveViewModel(),
        security = EspionageSecurityViewModel(random),
        advancement = AdvancementViewModel(),
        trade = TradeMarketViewModel(random),
        governance = GovernanceViewModel(random),
        demographics = DemographicsCampaignViewModel(),
        advanceDate = ::advanceSimulationDate,
        applyPopulationChange = ::applyPopulationPhysics,
        onStage = onStage,
    )
}

/** Fresh campaign for [seed], matching what `startNewGame("us", "standard")` produces. */
internal fun seededStartingState(seed: Long): GameState =
    ScenarioCatalog.apply(
        GameState.initial("us"),
        scenarioId = "standard",
        seed = seed.toInt(),
        challengeId = "standard",
    ).copy(campaignSeed = seed)
