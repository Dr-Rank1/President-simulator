package com.presidentsimulator.game.data

import com.presidentsimulator.game.viewmodel.AdvancementEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResearchEngineTest {

    private val engine = AdvancementEngine()
    private val tech = TechCatalog.byId("precision_agriculture")
        ?: error("precision_agriculture must exist in TechCatalog")

    private fun stateWithScience(points: Long): GameState =
        GameState(research = ResearchState(sciencePoints = points))

    private fun runUntilResearchCompletes(state: GameState, maxTicks: Int = 400): GameState {
        var current = state
        var ticks = 0
        while (current.research.activeTechId != null && ticks < maxTicks) {
            current = engine.processSocietyTick(current)
            ticks++
        }
        assertTrue("research never completed within $maxTicks ticks", current.research.activeTechId == null)
        return current
    }

    @Test
    fun completingResearchKeepsTheBankedScienceReserve() {
        val banked = 500L
        val started = engine.startResearch(stateWithScience(banked), tech.id)
        assertEquals(tech.id, started.research.activeTechId)
        assertEquals(banked, started.research.sciencePoints)

        val finished = runUntilResearchCompletes(started)

        assertTrue(finished.research.isUnlocked(tech.id))
        // Regression guard: completion used to overwrite the reserve with only the
        // leftover overflow, silently destroying every point the player had banked.
        assertTrue(
            "banked science was destroyed: ${finished.research.sciencePoints} < $banked",
            finished.research.sciencePoints >= banked,
        )
    }

    @Test
    fun instantUnlockSpendsExactlyTheScienceCost() {
        val finished = engine.unlockTechnology(stateWithScience(500L), tech.id)

        assertTrue(finished.research.isUnlocked(tech.id))
        assertEquals(500L - tech.scienceCost, finished.research.sciencePoints)
    }

    @Test
    fun scienceAccruesToTheReserveWhileNoProjectIsActive() {
        val before = stateWithScience(120L)
        val after = engine.processSocietyTick(before)

        assertEquals(null, after.research.activeTechId)
        assertTrue(
            "science should grow with no active project",
            after.research.sciencePoints > before.research.sciencePoints,
        )
    }

    @Test
    fun researchIsRejectedWhenPrerequisitesAreMissing() {
        val locked = TechCatalog.all.first { it.prerequisiteIds.isNotEmpty() }
        val started = engine.startResearch(stateWithScience(100_000L), locked.id)

        assertEquals(null, started.research.activeTechId)
        assertTrue(!started.research.isUnlocked(locked.id))
    }
}
