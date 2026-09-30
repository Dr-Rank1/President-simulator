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

/**
 * Applies one complete monthly simulation in a fixed, documented order.
 * The view model owns UI effects and summaries; this class owns state-to-state turn resolution.
 */
internal class MonthlySimulationPipeline(
    private val random: Random,
    private val diplomacy: DiplomacyViewModel,
    private val productionLaw: ProductionLawViewModel,
    private val analytics: AnalyticsSaveViewModel,
    private val security: EspionageSecurityViewModel,
    private val advancement: AdvancementViewModel,
    private val trade: TradeMarketViewModel,
    private val governance: GovernanceViewModel,
    private val demographics: DemographicsCampaignViewModel,
    private val advanceDate: (Int, Int) -> Pair<Int, Int>,
    private val applyPopulationChange: (GameState) -> Long,
) {
    fun advance(current: GameState): GameState {
        val (month, year) = advanceDate(current.month, current.year)
        var next = current.copy(month = month, year = year)

        // Paid equipment and personnel orders arrive before the monthly budget settles.
        next = diplomacy.processMilitaryProcurement(next)

        // 1. Production and fiscal settlement.
        next = productionLaw.processProductionTick(next)
        next = FiscalEngine.settleMonth(next).let { settled ->
            settled.copy(vitals = settled.vitals.copy(population = applyPopulationChange(settled)))
        }

        // 2. Foreign affairs, active war, internal security, then crisis aftermath.
        next = diplomacy.simulateGeopolitics(next)
        if (next.diplomacy.activeWar != null) next = diplomacy.simulateWarBattle(next)
        next = security.processSecurityTick(next)
        if (next.gameOver.isGameOver) return next
        next = processCrisisTick(next)

        // 3. Domestic political cycle and response pressure.
        next = PressDesk.processMonth(next, random)
        next = CabinetEngine.processMonth(next, random)
        next = OppositionEngine.processMonth(next, random)
        next = DisasterEngine.processMonth(next, random)
        next = SpeechEngine.tickCooldowns(next)
        next = TermEngine.processMonth(next)
        if (next.gameOver.isGameOver) return next

        // 3b. Domination layer: military industry, world spread, milestones.
        next = MilitaryIndustryEngine.processMonth(next)
        next = VictoryEngine.processMonth(next)
        next = VictoryEngine.maybeEmitMilestone(next)
        if (next.gameOver.isGameOver) return next

        // 4. Long-term progression, institutions, trade, and global governance.
        next = advancement.processSocietyTick(next)
        next = productionLaw.processLawsTick(next)
        next = PolicyImpactEngine.processMonth(next)
        next = trade.processTradeTick(next)
        next = governance.processGovernanceTick(next)
        next = demographics.processDemographicsTick(next)
        if (next.gameOver.isGameOver) return next

        // 5. Record history and publish the next presidential agenda.
        next = analytics.recordHistoricalSnapshot(next)
        next = LegacyLedger.processMonth(current, next)
        next = next.copy(agenda = AgendaBuilder.applyMonthlyAgenda(next.agenda, next))
        next = MandateEngine.processMonth(next)
        next = emitWorldNews(current, next)
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
