package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.AgendaBuilder
import com.presidentsimulator.game.data.CabinetEngine
import com.presidentsimulator.game.data.DisasterEngine
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.FiscalEngine
import com.presidentsimulator.game.data.LegacyLedger
import com.presidentsimulator.game.data.MandateEngine
import com.presidentsimulator.game.data.OppositionEngine
import com.presidentsimulator.game.data.PressDesk
import com.presidentsimulator.game.data.PolicyImpactEngine
import com.presidentsimulator.game.data.SpeechEngine
import com.presidentsimulator.game.data.StoryArcEngine
import com.presidentsimulator.game.data.TermEngine
import com.presidentsimulator.game.data.MilitaryIndustryEngine
import com.presidentsimulator.game.data.VictoryEngine
import com.presidentsimulator.game.data.pushNews
import kotlin.random.Random

/** Calendar physics shared by the live ticker and simulation tests. */
internal fun advanceSimulationDate(month: Int, year: Int): Pair<Int, Int> =
    if (month >= 12) 1 to (year + 1) else (month + 1) to year

/**
 * Housing- and approval-driven population dynamics shared by the live ticker and
 * simulation tests, so both see identical growth rules.
 */
internal fun applyPopulationPhysics(state: GameState): Long {
    val housingCapacity = state.economy.housing * 1_500_000L
    val population = state.vitals.population
    val approval = state.vitals.approval

    val housingFactor = when {
        population < housingCapacity * 0.85 -> 1.002
        population > housingCapacity -> 0.997
        else -> 1.0005
    }
    val approvalFactor = when {
        approval >= 60f -> 1.001
        approval <= 30f -> 0.998
        else -> 1.0
    }

    return (population * housingFactor * approvalFactor)
        .toLong()
        .coerceAtLeast(1_000_000L)
}

/**
 * Applies one complete monthly simulation in a fixed, documented order.
 * The view model owns UI effects and summaries; this class owns state-to-state turn resolution.
 *
 * [onStage] is an observability hook: it receives the name of each stage as it runs so
 * tests can pin the documented turn order. It must stay adjacent to its stage call.
 */
internal class MonthlySimulationPipeline(
    private val random: Random,
    private val diplomacy: DiplomacyEngine,
    private val productionLaw: ProductionLawEngine,
    private val analytics: AnalyticsSaveEngine,
    private val security: EspionageSecurityEngine,
    private val advancement: AdvancementEngine,
    private val trade: TradeMarketEngine,
    private val governance: GovernanceEngine,
    private val demographics: DemographicsCampaignEngine,
    private val advanceDate: (Int, Int) -> Pair<Int, Int>,
    private val applyPopulationChange: (GameState) -> Long,
    private val onStage: (String) -> Unit = {},
) {
    fun advance(current: GameState): GameState {
        onStage("date")
        val (month, year) = advanceDate(current.month, current.year)
        var next = current.copy(month = month, year = year)

        // Paid equipment and personnel orders arrive before the monthly budget settles.
        onStage("procurement")
        next = diplomacy.processMilitaryProcurement(next)

        // 1. Production and fiscal settlement.
        onStage("production")
        next = productionLaw.processProductionTick(next)
        onStage("fiscal")
        next = FiscalEngine.settleMonth(next).let { settled ->
            settled.copy(vitals = settled.vitals.copy(population = applyPopulationChange(settled)))
        }

        // 2. Foreign affairs, active war, internal security, then crisis aftermath.
        onStage("geopolitics")
        next = diplomacy.simulateGeopolitics(next)
        if (next.diplomacy.activeWar != null) {
            onStage("war")
            next = diplomacy.simulateWarBattle(next)
        }
        onStage("security")
        next = security.processSecurityTick(next)
        if (next.gameOver.isGameOver) return next
        onStage("crisis")
        next = processCrisisTick(next)

        // 3. Domestic political cycle and response pressure.
        onStage("press")
        next = PressDesk.processMonth(next, random)
        onStage("cabinet")
        next = CabinetEngine.processMonth(next, random)
        onStage("opposition")
        next = OppositionEngine.processMonth(next, random)
        onStage("disaster")
        next = DisasterEngine.processMonth(next, random)
        onStage("speech_cooldowns")
        next = SpeechEngine.tickCooldowns(next)
        onStage("term")
        next = TermEngine.processMonth(next)
        if (next.gameOver.isGameOver) return next

        // 3b. Domination layer: military industry, world spread, milestones.
        onStage("military_industry")
        next = MilitaryIndustryEngine.processMonth(next)
        onStage("victory")
        next = VictoryEngine.processMonth(next)
        onStage("victory_milestone")
        next = VictoryEngine.maybeEmitMilestone(next)
        if (next.gameOver.isGameOver) return next

        // 4. Long-term progression, institutions, trade, and global governance.
        onStage("society")
        next = advancement.processSocietyTick(next)
        onStage("laws")
        next = productionLaw.processLawsTick(next)
        onStage("policy_impact")
        next = PolicyImpactEngine.processMonth(next)
        onStage("trade")
        next = trade.processTradeTick(next)
        onStage("governance")
        next = governance.processGovernanceTick(next)
        onStage("demographics")
        next = demographics.processDemographicsTick(next)
        if (next.gameOver.isGameOver) return next

        // 5. Record history and publish the next presidential agenda.
        onStage("history")
        next = analytics.recordHistoricalSnapshot(next)
        onStage("legacy")
        next = LegacyLedger.processMonth(current, next)
        onStage("agenda")
        next = next.copy(agenda = AgendaBuilder.applyMonthlyAgenda(next.agenda, next))
        onStage("mandate")
        next = MandateEngine.processMonth(next)
        onStage("news")
        next = emitWorldNews(current, next)
        onStage("story_arc")
        return StoryArcEngine.onMonth(next)
    }

    /** Keeps the news ticker alive with world happenings. */
    private fun emitWorldNews(before: GameState, after: GameState): GameState {
        var next = after
        val war = next.diplomacy.activeWar
        val warBefore = before.diplomacy.activeWar
        if (war != null && warBefore == null) {
            val target = next.diplomacy.rivalById(war.targetCountryId)?.name ?: war.targetCountryId
            next = next.pushNews("WAR: ${next.playerNation.name} declares war on $target", tag = "WAR")
        }
        if (war != null && war.monthsActive > 0 && war.monthsActive % 6 == 0 &&
            warBefore?.monthsActive != war.monthsActive
        ) {
            val target = next.diplomacy.rivalById(war.targetCountryId)?.name ?: war.targetCountryId
            next = next.pushNews("War with $target enters month ${war.monthsActive}", tag = "WAR")
        }
        val electionNear = next.nextElectionYear > 0 && next.nextElectionYear - next.year == 1 && next.month == 1
        if (electionNear && before.nextElectionYear == next.nextElectionYear) {
            next = next.pushNews("Elections scheduled for ${next.nextElectionYear} — campaigns intensify", tag = "POLITICS")
        }
        VictoryEngine.spreadHeadline(next)?.let { headline ->
            if (next.month % 3 == 1) next = next.pushNews(headline, tag = "DOMINION")
        }
        return next
    }

    private fun processCrisisTick(state: GameState): GameState {
        val crisis = state.crisis
        if (crisis.lingeringMonths <= 0) {
            if (crisis.eventCooldownMonths <= 0) return state
            return state.copy(crisis = crisis.copy(eventCooldownMonths = crisis.eventCooldownMonths - 1))
        }
        val remaining = crisis.lingeringMonths - 1
        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget + crisis.monthlyBudgetDelta,
                approval = (state.vitals.approval + crisis.monthlyApprovalDelta).coerceIn(0f, 100f),
            ),
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (state.internalSecurity.instabilityScore + crisis.monthlyInstabilityDelta)
                    .coerceIn(0f, 100f),
            ),
            crisis = if (remaining <= 0) {
                crisis.copy(
                    lingeringMonths = 0,
                    monthlyApprovalDelta = 0f,
                    monthlyInstabilityDelta = 0f,
                    monthlyBudgetDelta = 0L,
                    label = "",
                )
            } else {
                crisis.copy(lingeringMonths = remaining)
            },
        )
    }
}
