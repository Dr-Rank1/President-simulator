package com.presidentsimulator.game.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CabinetEngineTest {

    private fun stateWith(
        budget: Long = 100_000_000_000L,
        approval: Float = 50f,
        seed: Long = 7L,
    ): GameState {
        val base = GameState(vitals = VitalsState(budget = budget, approval = approval))
        return base.copy(cabinet = CabinetEngine.seedInitial(Random(seed)))
    }

    @Test
    fun firingAMinisterCostsMoneyOpensASeatAndRunsCooldown() {
        val before = stateWith()
        val target = before.cabinet.ministers.first()
        val fired = CabinetEngine.fireMinister(before, target.portfolio)

        assertEquals(before.vitals.budget - CabinetEngine.FIRE_COST, fired.vitals.budget)
        assertEquals(CabinetEngine.FIRE_COOLDOWN, fired.cabinet.fireCooldownMonths)
        assertEquals(before.cabinet.ministers.size - 1, fired.cabinet.ministers.size)
        assertTrue(fired.cabinet.ministerFor(target.portfolio) == null)
        assertTrue(fired.cabinet.firingsThisTerm == before.cabinet.firingsThisTerm + 1)
        assertTrue("cohesion never negative", fired.cabinet.cohesion >= 0f)
        assertTrue("approval ${fired.vitals.approval}", fired.vitals.approval < before.vitals.approval)
        assertTrue("instability stays clamped", fired.internalSecurity.instabilityScore in 0f..100f)
    }

    @Test
    fun firingIsRefusedDuringCooldownOrWithoutBudget() {
        val cooling = stateWith().let { it.copy(cabinet = it.cabinet.copy(fireCooldownMonths = 1)) }
        val target = cooling.cabinet.ministers.first()
        assertEquals(cooling, CabinetEngine.fireMinister(cooling, target.portfolio))

        val broke = stateWith(budget = CabinetEngine.FIRE_COST - 1)
        val brokeTarget = broke.cabinet.ministers.first()
        assertEquals(broke, CabinetEngine.fireMinister(broke, brokeTarget.portfolio))
    }

    /** Vacates the first portfolio and builds a candidate pool for that seat. */
    private fun stateWithVacancy(): Triple<GameState, MinisterCandidate, Minister> {
        val seeded = stateWith()
        val removed = seeded.cabinet.ministers.first()
        val vacated = seeded.copy(
            cabinet = seeded.cabinet.copy(
                ministers = seeded.cabinet.ministers.filter { it.portfolio != removed.portfolio },
            ),
        )
        val withPool = vacated.copy(
            cabinet = CabinetEngine.refreshCandidates(vacated.cabinet, Random(5L)),
        )
        val candidate = withPool.cabinet.candidates.first { it.portfolio == removed.portfolio }
        return Triple(withPool, candidate, removed)
    }

    @Test
    fun appointingFillsTheVacantSeatAndChargesTheHireCost() {
        val (vacant, candidate, _) = stateWithVacancy()

        val appointed = CabinetEngine.appointCandidate(vacant, candidate.id)

        assertEquals(vacant.vitals.budget - candidate.hireCost, appointed.vitals.budget)
        assertTrue(appointed.cabinet.ministerFor(candidate.portfolio) != null)
        assertEquals(CabinetEngine.APPOINT_COOLDOWN, appointed.cabinet.appointCooldownMonths)
        assertTrue(appointed.cabinet.candidates.none { it.id == candidate.id })
        assertTrue("cohesion bounded", appointed.cabinet.cohesion <= 100f)
    }

    @Test
    fun appointmentIsRefusedWhenTheSeatIsTakenOrDuringCooldown() {
        val (vacant, candidate, removed) = stateWithVacancy()

        // The seat got filled by someone else in the meantime.
        val filledAgain = vacant.copy(
            cabinet = vacant.cabinet.copy(ministers = vacant.cabinet.ministers + removed),
        )
        assertEquals(filledAgain, CabinetEngine.appointCandidate(filledAgain, candidate.id))

        // Cooldown from a previous appointment blocks the swap.
        val cooling = vacant.copy(
            cabinet = vacant.cabinet.copy(appointCooldownMonths = 1),
        )
        assertEquals(cooling, CabinetEngine.appointCandidate(cooling, candidate.id))
    }

    @Test
    fun monthlyCycleKeepsCabinetAndVitalsClamped() {
        var state = stateWith(approval = 0f)
        val random = Random(4242L)
        repeat(60) {
            state = CabinetEngine.processMonth(state, random)
            assertTrue("cohesion ${state.cabinet.cohesion}", state.cabinet.cohesion in 0f..100f)
            state.cabinet.ministers.forEach { minister ->
                assertTrue("loyalty ${minister.loyalty}", minister.loyalty in 5f..100f)
                assertTrue("competence ${minister.competence}", minister.competence in 15f..98f)
                assertTrue("heat ${minister.scandalHeat}", minister.scandalHeat in 0f..100f)
            }
            assertTrue("approval ${state.vitals.approval}", state.vitals.approval in 0f..100f)
            assertTrue(
                "instability ${state.internalSecurity.instabilityScore}",
                state.internalSecurity.instabilityScore in 0f..100f,
            )
            assertTrue("budget never negative", state.vitals.budget >= 0L)
        }
    }
}
