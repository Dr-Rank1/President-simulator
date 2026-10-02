package com.presidentsimulator.game.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class OppositionEngineTest {

    private fun stateWith(
        budget: Long = 100_000_000_000L,
        approval: Float = 50f,
        seed: Long = 99L,
    ): GameState {
        val base = GameState(vitals = VitalsState(budget = budget, approval = approval))
        return base.copy(opposition = OppositionEngine.seedInitial(Ideology.AUTOCRACY, Random(seed)))
    }

    @Test
    fun negotiateSoftensTheMainOppositionAndRunsCooldown() {
        val before = stateWith()
        val mainBefore = before.opposition.mainOpposition!!
        val after = OppositionEngine.negotiate(before)

        assertEquals(before.vitals.budget - OppositionEngine.NEGOTIATE_COST, after.vitals.budget)
        assertEquals(4, after.opposition.negotiateCooldownMonths)
        val mainAfter = after.opposition.parties.first { it.id == mainBefore.id }
        assertTrue(
            "hostility must drop (was ${mainBefore.hostility}, now ${mainAfter.hostility})",
            mainAfter.hostility < mainBefore.hostility,
        )
        assertTrue(mainAfter.hostility >= 5f)
        assertTrue(
            "no-confidence heat must drop",
            after.opposition.noConfidenceHeat <= before.opposition.noConfidenceHeat,
        )
        assertTrue(after.opposition.noConfidenceHeat >= 0f)
        assertTrue(after.vitals.approval in 0f..100f)
    }

    @Test
    fun negotiateIsRefusedDuringCooldownOrWithoutBudget() {
        val cooling = stateWith().let {
            it.copy(opposition = it.opposition.copy(negotiateCooldownMonths = 2))
        }
        assertEquals(cooling, OppositionEngine.negotiate(cooling))

        val broke = stateWith(budget = OppositionEngine.NEGOTIATE_COST - 1)
        assertEquals(broke, OppositionEngine.negotiate(broke))
    }

    @Test
    fun smearLandsOrBackfiresButNeverBreaksClamps() {
        var sawLanding = false
        var sawBackfire = false
        repeat(60) { attempt ->
            val before = stateWith(seed = attempt.toLong())
            val after = OppositionEngine.smear(before, Random(attempt.toLong()))

            assertEquals(before.vitals.budget - OppositionEngine.SMEAR_COST, after.vitals.budget)
            assertEquals(3, after.opposition.smearCooldownMonths)
            assertTrue("approval ${after.vitals.approval}", after.vitals.approval in 0f..100f)
            assertTrue(
                "sentiment ${after.press.mediaSentiment}",
                after.press.mediaSentiment in 0f..100f,
            )
            assertTrue("credibility floor", after.press.credibility >= 10f)
            assertTrue(
                "momentum clamp ${after.demographics.oppositionMomentum}",
                after.demographics.oppositionMomentum in 0f..40f,
            )
            after.opposition.parties.forEach { party ->
                assertTrue("hostility ${party.hostility}", party.hostility in 0f..100f)
                assertTrue("popularity ${party.popularity}", party.popularity in 0f..100f)
            }
            if (after.opposition.lastPlayerCounter == "Smear backfired.") sawBackfire = true
            if (after.opposition.lastPlayerCounter == "Smear campaign landed.") sawLanding = true
        }
        assertTrue("both smear outcomes must be reachable", sawLanding && sawBackfire)
    }

    @Test
    fun concessionsLowerHostilityWithinClamps() {
        val before = stateWith()
        val mainBefore = before.opposition.mainOpposition!!
        val after = OppositionEngine.concedePlatform(before)

        assertEquals(before.vitals.budget - OppositionEngine.CONCESSION_COST, after.vitals.budget)
        assertEquals(3, after.opposition.concessionCooldownMonths)
        val mainAfter = after.opposition.parties.first { it.id == mainBefore.id }
        assertTrue(mainAfter.hostility < mainBefore.hostility)
        assertTrue(mainAfter.popularity >= 5f)
        assertTrue(after.vitals.approval in 0f..100f)
        assertTrue(after.demographics.workingClass in 0f..100f)
    }

    @Test
    fun monthlyTacticsKeepOppositionStateClamped() {
        var state = stateWith(approval = 0f)
        val random = Random(1337L)
        repeat(60) {
            state = OppositionEngine.processMonth(state, random)
            assertTrue(
                "heat ${state.opposition.noConfidenceHeat}",
                state.opposition.noConfidenceHeat in 0f..100f,
            )
            assertTrue(
                "filibuster ${state.opposition.filibusterMonths}",
                state.opposition.filibusterMonths in 0..4,
            )
            state.opposition.parties.forEach { party ->
                assertTrue("hostility ${party.hostility}", party.hostility in 0f..100f)
                assertTrue("popularity ${party.popularity}", party.popularity in 0f..100f)
            }
            assertTrue("approval ${state.vitals.approval}", state.vitals.approval in 0f..100f)
            assertTrue(
                "instability ${state.internalSecurity.instabilityScore}",
                state.internalSecurity.instabilityScore in 0f..100f,
            )
        }
    }
}
