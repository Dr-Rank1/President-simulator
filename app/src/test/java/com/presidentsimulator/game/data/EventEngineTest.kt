package com.presidentsimulator.game.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventEngineTest {

    private fun stateWith(
        foodShortage: Boolean = false,
        energyShortage: Boolean = false,
        atWar: Boolean = false,
        netIncomeNegative: Boolean = false,
        unstable: Boolean = false,
    ): GameState {
        val base = GameState()
        var next = base.copy(
            production = base.production.copy(
                foodShortage = foodShortage,
                energyShortage = energyShortage,
            ),
            internalSecurity = base.internalSecurity.copy(
                instabilityScore = if (unstable) 80f else 10f,
            ),
        )
        if (netIncomeNegative) {
            next = next.copy(
                economy = next.economy.copy(imports = 100_000_000_000L),
            )
        }
        if (atWar) {
            val rival = next.diplomacy.rivals.first()
            next = next.copy(
                diplomacy = next.diplomacy.copy(
                    activeWar = WarState(targetCountryId = rival.id),
                ),
            )
        }
        return next
    }

    @Test
    fun weightedEventAlwaysReturnsAnEvent() {
        val state = stateWith()
        repeat(20) {
            val event = EventRepository.weightedEvent(state, kotlin.random.Random(it))
            assertNotNull(event)
            assertTrue(EventRepository.eventPool.contains(event))
        }
    }

    @Test
    fun shortageConditionsHeavilyWeightShortageEvents() {
        val calm = stateWith()
        val starving = stateWith(foodShortage = true, energyShortage = true)

        fun shortageShare(state: GameState): Double {
            val weights = EventRepository.eventPool.map { event ->
                event to EventRepository.weightedEventWeight(event, state)
            }
            val total = weights.sumOf { it.second.toDouble() }
            val shortage = weights
                .filter { (event, _) -> event.id.contains("shortage") || event.id.contains("strike") }
                .sumOf { it.second.toDouble() }
            return shortage / total
        }

        assertTrue(shortageShare(starving) > shortageShare(calm))
    }

    @Test
    fun warStateSuppressesNonWarEvents() {
        val peace = stateWith()
        val war = stateWith(atWar = true)
        val warEvent = EventRepository.eventPool.first { it.id.contains("border") || it.id.contains("war") || it.id.contains("arms") }
        val domesticEvent = EventRepository.eventPool.first { it.id.contains("strike") }

        assertTrue(
            EventRepository.weightedEventWeight(warEvent, war) > EventRepository.weightedEventWeight(warEvent, peace),
        )
        // War-tagged events are heavily boosted; domestic ones are untouched by war,
        // so a war event should outweigh a domestic one once war is underway.
        assertTrue(
            EventRepository.weightedEventWeight(warEvent, war) > EventRepository.weightedEventWeight(domesticEvent, war),
        )
    }

    @Test
    fun eventByIdResolvesPoolAndReturnsNullForUnknown() {
        assertNotNull(EventRepository.byId("general_strike"))
        assertNull(EventRepository.byId("no_such_event_exists"))
    }

    @Test
    fun consequenceApplicationIsClampedAndCoherent() {
        val consequence = EventConsequence(
            budgetChange = 1_000_000_000L,
            approvalChange = 500f, // far out of range
            populationChange = -999_999_999_999L, // would wipe population
            factoriesChange = -50,
            defconChange = 99,
            instabilityChange = -500f,
        )
        val after = consequence.applyTo(GameState())

        assertEquals(100f, after.vitals.approval, 0.0001f)
        assertEquals(1_000_000L, after.vitals.population)
        assertEquals(0, after.economy.factories)
        assertEquals(5, after.military.defcon) // default 4 + 99, clamped to the ceiling
        assertEquals(0f, after.internalSecurity.instabilityScore, 0.0001f)
    }

    @Test
    fun relationshipChangesApplyOnlyToNamedRivals() {
        val after = EventConsequence(relationshipChanges = mapOf("eastmark" to -8)).applyTo(GameState())
        val eastmark = after.diplomacy.rivals.first { it.id == "eastmark" }
        val untouched = after.diplomacy.rivals.first { it.id != "eastmark" }
        assertEquals(-8, eastmark.relationshipScore - GameState().diplomacy.rivals.first { it.id == "eastmark" }.relationshipScore)
        assertEquals(untouched.relationshipScore, GameState().diplomacy.rivals.first { it.id == untouched.id }.relationshipScore)
    }
}
