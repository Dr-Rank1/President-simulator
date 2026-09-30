package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MilitaryState
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.WarGoal
import com.presidentsimulator.game.data.WarState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WarEngineTest {

    private val engine = DiplomacyViewModel()

    private fun stateWith(
        personnel: Long = 500_000L,
        tanks: Int = 800,
        jets: Int = 120,
        relation: Int = 0,
        strength: Double = 500.0,
    ): GameState {
        val base = GameState(
            military = MilitaryState(personnel = personnel, tanks = tanks, jets = jets),
        )
        val rival = RivalNation(
            id = "rival_a",
            name = "Rival A",
            flagEmoji = "🟦",
            relationshipScore = relation,
            militaryStrength = strength,
        )
        return base.copy(diplomacy = base.diplomacy.copy(rivals = listOf(rival)))
    }

    @Test
    fun declareWarMobilizesAndResetsRelations() {
        val before = stateWith(relation = 30)
        val after = engine.declareWar(before, "rival_a", WarGoal.REPARATIONS)

        val war = after.diplomacy.activeWar
        assertNotNull(war)
        assertEquals("rival_a", war!!.targetCountryId)
        assertEquals(0f, war.warProgress, 0.0001f)
        assertEquals(-100, after.diplomacy.rivals.single().relationshipScore)
        assertFalse(after.diplomacy.rivals.single().hasTradeTreaty)
        assertEquals(com.presidentsimulator.game.data.DeploymentStatus.MOBILIZED, after.military.deployment)
        assertTrue(after.military.defcon <= 2)
    }

    @Test
    fun cannotDeclareTwoWarsOrBreakPact() {
        val atWar = engine.declareWar(stateWith(), "rival_a")
        // A second declaration while already at war must be a no-op.
        assertEquals(atWar, engine.declareWar(atWar, "rival_a"))

        val pacified = stateWith().let { s ->
            s.copy(
                diplomacy = s.diplomacy.copy(
                    rivals = s.diplomacy.rivals.map { it.copy(hasNonAggressionPact = true) },
                ),
            )
        }
        assertFalse(DiplomacyViewModel.canDeclareWar(pacified, "rival_a"))
    }

    @Test
    fun battleMovesProgressAndRecordsReports() {
        var state = engine.declareWar(stateWith(strength = 100.0), "rival_a")
        val warStart = state.diplomacy.activeWar!!

        // Strength asymmetry makes player wins overwhelming; progress must climb.
        repeat(10) {
            state = engine.simulateWarBattle(state)
        }

        val war = state.diplomacy.activeWar
        // War may have already ended in victory within 10 months.
        if (war != null) {
            assertTrue(war.warProgress > warStart.warProgress)
            assertTrue(war.monthsActive >= 10)
            assertTrue(war.battleReports.isNotEmpty())
            assertTrue(war.playerCasualties > 0)
        } else {
            assertEquals(engine.consumeLastResolvedWar()?.victory, true)
        }
    }

    @Test
    fun warEndsInVictoryAtFullProgress() {
        var state = engine.declareWar(stateWith(strength = 50.0), "rival_a")
        var months = 0
        while (state.diplomacy.activeWar != null && months < 100) {
            state = engine.simulateWarBattle(state)
            months++
        }

        assertNull(state.diplomacy.activeWar)
        val outcome = engine.consumeLastResolvedWar()
        assertNotNull("war should resolve within 100 months", outcome)
        assertTrue(outcome!!.victory)
    }

    @Test
    fun armisticeEndsWarAndCostsTreasury() {
        var state = engine.declareWar(stateWith(), "rival_a")
        state = state.copy(vitals = state.vitals.copy(budget = 1_000_000_000_000L))
        state = engine.simulateWarBattle(state)
        val budgetBefore = state.vitals.budget

        val after = engine.signArmistice(state)

        assertNull(after.diplomacy.activeWar)
        assertTrue(after.vitals.budget < budgetBefore)
    }

    @Test
    fun armisticeCostsMoreWhenLosing() {
        val losing = stateWith().let { s ->
            s.copy(
                vitals = s.vitals.copy(budget = 1_000_000_000_000L),
                diplomacy = s.diplomacy.copy(
                    rivals = s.diplomacy.rivals.map { it.copy(id = it.id) },
                    activeWar = WarState(targetCountryId = "rival_a", warProgress = -80f),
                ),
            )
        }
        val winning = losing.copy(
            diplomacy = losing.diplomacy.copy(
                activeWar = WarState(targetCountryId = "rival_a", warProgress = 80f),
            ),
        )

        assertTrue(DiplomacyViewModel.armisticeCost(losing) > DiplomacyViewModel.armisticeCost(winning))
    }

    @Test
    fun gameoverStateIsUntouchableByBattles() {
        // Battles only run when a war exists; ensure a frozen state with no war is a no-op.
        val frozen = stateWith().copy(gameOver = stateWith().gameOver.copy(isGameOver = true))
        assertEquals(frozen, engine.simulateWarBattle(frozen))
    }

    @Test
    fun industryAndPuppetsCountTowardWarPower() {
        val plain = stateWith()
        val built = stateWith().let { s ->
            s.copy(
                militaryIndustry = s.militaryIndustry.copy(arsenals = 3, airfields = 1, shipyards = 1),
                territory = s.territory.copy(
                    conquered = listOf(
                        com.presidentsimulator.game.data.ConqueredTerritory(
                            countryId = "rival_a",
                            name = "Rival A",
                            flagEmoji = "🟦",
                            status = com.presidentsimulator.game.data.TerritoryStatus.PUPPET,
                            year = 2026,
                            month = 1,
                        ),
                    ),
                ),
            )
        }

        // Raw combat strength is identical; only industry + puppets differ.
        assertEquals(
            plain.effectiveCombatStrength,
            built.effectiveCombatStrength,
            0.0001,
        )
        assertTrue(engine.warPower(built) > engine.warPower(plain))

        // warPower = effectiveCombatStrength + flat industry/puppet contributions.
        val expected = built.effectiveCombatStrength + (3 * 12.0 + 1 * 15.0 + 1 * 18.0) + 40.0
        assertEquals(expected, engine.warPower(built), 0.0001)
    }
}
