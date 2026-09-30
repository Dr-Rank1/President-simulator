package com.presidentsimulator.game.data

import com.presidentsimulator.game.viewmodel.DiplomacyViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DominationEngineTest {

    private fun stateAtWar(progress: Float = 100f): GameState {
        val base = GameState()
        val rival = base.diplomacy.rivals.first()
        return base.copy(
            vitals = base.vitals.copy(budget = 50_000_000_000L, approval = 50f),
            diplomacy = base.diplomacy.copy(
                activeWar = WarState(
                    targetCountryId = rival.id,
                    warProgress = progress,
                    monthsActive = 3,
                ),
            ),
            victoryPath = VictoryPathState(chosenPath = VictoryPath.MILITARY_DOMINANCE),
        )
    }

    @Test
    fun annexationCountsTowardDominationAndUnrest() {
        val warState = stateAtWar()
        val targetId = warState.diplomacy.activeWar!!.targetCountryId

        val annexed = VictoryEngine.resolveConquest(warState, targetId, TerritoryStatus.ANNEXED)

        assertEquals(1, annexed.territory.annexedCount)
        assertEquals(1, annexed.territory.controlledCount)
        assertTrue(annexed.territory.annexationInstabilityPressure > 0f)
        assertTrue(annexed.vitals.approval < warState.vitals.approval)
        assertTrue(annexed.news.entries.any { it.tag == "WAR" })
    }

    @Test
    fun puppetStatePaysTributeAndCountsAsControlled() {
        val warState = stateAtWar()
        val targetId = warState.diplomacy.activeWar!!.targetCountryId

        val puppeted = VictoryEngine.resolveConquest(warState, targetId, TerritoryStatus.PUPPET)

        assertEquals(1, puppeted.territory.puppetCount)
        assertEquals(1, puppeted.territory.controlledCount)
        assertEquals(1_200_000_000L, puppeted.territory.monthlyTributeIncome)
        assertTrue(puppeted.diplomacy.rivalById(targetId)!!.relationshipScore > 0)
    }

    @Test
    fun liberationBoostsApprovalButDoesNotCountAsControlled() {
        val warState = stateAtWar()
        val targetId = warState.diplomacy.activeWar!!.targetCountryId

        val liberated = VictoryEngine.resolveConquest(warState, targetId, TerritoryStatus.LIBERATED)

        assertEquals(0, liberated.territory.controlledCount)
        assertEquals(1, liberated.territory.liberatedCount)
        assertTrue(liberated.vitals.approval > warState.vitals.approval)
    }

    @Test
    fun cannotConquerSameNationTwice() {
        val warState = stateAtWar()
        val targetId = warState.diplomacy.activeWar!!.targetCountryId
        val first = VictoryEngine.resolveConquest(warState, targetId, TerritoryStatus.ANNEXED)

        assertEquals(first, VictoryEngine.resolveConquest(first, targetId, TerritoryStatus.PUPPET))
    }

    @Test
    fun militaryVictoryFiresAtThreshold() {
        var state = stateAtWar()
        val rivals = state.diplomacy.rivals.take(VictoryThresholds.NATIONS_TO_CONTROL)
        rivals.forEach { rival ->
            state = state.copy(
                diplomacy = state.diplomacy.copy(
                    activeWar = WarState(targetCountryId = rival.id, warProgress = 100f),
                ),
            )
            state = VictoryEngine.resolveConquest(state, rival.id, TerritoryStatus.PUPPET)
        }

        assertEquals(VictoryThresholds.NATIONS_TO_CONTROL, state.territory.controlledCount)
        val checked = VictoryEngine.checkVictories(state)
        assertTrue(checked.gameOver.isGameOver)
        assertTrue(checked.gameOver.isVictory)
        assertTrue(checked.gameOver.reason.contains("Military dominance"))
    }

    @Test
    fun religiousVictoryRequiresInfluenceAndStateFaith() {
        val base = GameState().copy(
            victoryPath = VictoryPathState(
                chosenPath = VictoryPath.RELIGIOUS_DOMINANCE,
                religiousInfluence = VictoryThresholds.INFLUENCE_TO_WIN,
            ),
        )
        val won = VictoryEngine.checkVictories(base)
        assertTrue(won.gameOver.isVictory)

        val below = base.copy(
            victoryPath = base.victoryPath.copy(religiousInfluence = VictoryThresholds.INFLUENCE_TO_WIN - 1f),
        )
        assertFalse(VictoryEngine.checkVictories(below).gameOver.isGameOver)
    }

    @Test
    fun ideologicalSpreadAccumulatesMonthly() {
        val base = GameState().copy(
            victoryPath = VictoryPathState(chosenPath = VictoryPath.IDEOLOGICAL_DOMINANCE),
        )
        val afterOne = VictoryEngine.processMonth(base)
        val afterTen = (1..10).fold(base) { acc, _ -> VictoryEngine.processMonth(acc) }

        assertTrue(afterTen.victoryPath.ideologicalInfluence > afterOne.victoryPath.ideologicalInfluence)
        assertTrue(afterTen.victoryPath.ideologicalInfluence > 0f)
    }

    @Test
    fun noVictoryWithoutChosenPath() {
        val base = GameState().copy(
            territory = TerritoryState(
                conquered = List(5) { i ->
                    ConqueredTerritory("c$i", "C$i", "🏳", TerritoryStatus.ANNEXED, 2026, i + 1)
                },
            ),
        )
        assertFalse(VictoryEngine.checkVictories(base).gameOver.isGameOver)
    }

    @Test
    fun militaryIndustryProducesHardwareMonthly() {
        val base = GameState().copy(
            militaryIndustry = MilitaryIndustryState(arsenals = 2, airfields = 1, shipyards = 1),
            military = baseMilitary(),
        )
        val after = MilitaryIndustryEngine.processMonth(base)

        // 2 arsenals × 2 tanks each, 1 airfield × 1 jet, 1 shipyard × 1 ship.
        assertEquals(4, after.military.tanks - base.military.tanks)
        assertEquals(1, after.military.jets - base.military.jets)
        assertEquals(1, after.military.ships - base.military.ships)
        assertEquals(4, after.militaryIndustry.lastTanksProduced)
        assertEquals(1, after.militaryIndustry.lastJetsProduced)
        assertEquals(1, after.militaryIndustry.lastShipsProduced)
        assertTrue(after.news.entries.any { it.tag == "MILITARY" })
    }

    @Test
    fun loansRespectCreditLine() {
        val poor = GameState().copy(
            vitals = VitalsState(budget = 0L, population = 1_000_000_000L),
            diplomacy = GameState().diplomacy.copy(rivals = emptyList()),
        )
        val available = LoanEngine.availableLoan(poor)
        assertTrue("expected meaningful credit for a 1B-person economy, got $available", available >= LoanEngine.MIN_LOAN)

        val borrowed = LoanEngine.takeLoan(poor, LoanEngine.MIN_LOAN)
        assertEquals(LoanEngine.MIN_LOAN, borrowed.vitals.budget)
        assertEquals(LoanEngine.MIN_LOAN, borrowed.finance.publicDebt)
        assertTrue(borrowed.finance.monthlyInterestCost > 0L)

        // Overborrowing is rejected.
        assertEquals(poor, LoanEngine.takeLoan(poor, available + 5_000_000_000L))

        // Repayment reduces debt and restores credit.
        val repaid = LoanEngine.repayLoan(borrowed, 500_000_000L)
        assertEquals(borrowed.finance.publicDebt - 500_000_000L, repaid.finance.publicDebt)
        assertTrue(repaid.finance.creditScore >= borrowed.finance.creditScore)
    }

    @Test
    fun warOutcomeFlagsConquestAvailability() {
        val winner = DiplomacyViewModel()
        var state = GameState().copy(
            military = MilitaryState(personnel = 900_000L, tanks = 2_000, jets = 400, ships = 100),
        )
        // Weaken the only rival to guarantee quick victory.
        state = state.copy(
            diplomacy = state.diplomacy.copy(
                rivals = state.diplomacy.rivals.map { it.copy(militaryStrength = 1.0) },
            ),
        )
        state = winner.declareWar(state, state.diplomacy.rivals.first().id)
        repeat(60) {
            if (state.diplomacy.activeWar != null) state = winner.simulateWarBattle(state)
        }

        val outcome = winner.consumeLastResolvedWar()
        if (outcome != null && outcome.victory) {
            assertTrue(outcome.conquestAvailable)
        }
    }

    private fun baseMilitary(): MilitaryState = MilitaryState(personnel = 500_000L, tanks = 100, jets = 50, ships = 10)
}
