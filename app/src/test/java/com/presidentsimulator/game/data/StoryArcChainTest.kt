package com.presidentsimulator.game.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers §5.2 of the roadmap: chained, branching story events and the state-aware
 * event weights for systems that previously had no events at all.
 */
class StoryArcChainTest {

    private fun friendlyState(): GameState {
        val base = GameState()
        return base.copy(
            diplomacy = base.diplomacy.copy(
                rivals = base.diplomacy.rivals.map { it.copy(relationshipScore = 50) },
            ),
        )
    }

    private fun hostileState(): GameState {
        val base = GameState()
        return base.copy(
            diplomacy = base.diplomacy.copy(
                rivals = base.diplomacy.rivals.mapIndexed { index, rival ->
                    if (index == 0) rival.copy(relationshipScore = -70) else rival
                },
            ),
        )
    }

    @Test
    fun borderEscalationArcTriggersOnlyOnHostileRival() {
        assertEquals(
            null,
            StoryArcEngine.onMonth(friendlyState()).storyArc.activeArcId,
        )

        val started = StoryArcEngine.onMonth(hostileState())
        assertEquals("border_escalation", started.storyArc.activeArcId)
        assertEquals(1, started.storyArc.chapter)

        val firstEvent = StoryArcEngine.nextEvent(started)
        assertNotNull(firstEvent)
        assertEquals("story_border_escalation_1", firstEvent!!.id)
    }

    @Test
    fun borderEscalationChainsThroughThreeChaptersAndCloses() {
        var state = StoryArcEngine.onMonth(hostileState())

        // Chapter 1 — opening choice sets the branch ("open" = diplomatic).
        val chapter1 = StoryArcEngine.nextEvent(state)!!
        assertEquals("Shots Across the Border", chapter1.title)
        state = StoryArcEngine.resolve(state, chapter1.id, 0)
        assertEquals(2, state.storyArc.chapter)
        assertEquals(1, state.storyArc.monthsUntilNextChapter)
        assertEquals("Envoys return to the table", state.storyArc.lastStoryNote)
        assertNull("next chapter waits a month", StoryArcEngine.nextEvent(state))

        // Chapter 2 — branch-aware title and consequences.
        state = StoryArcEngine.onMonth(state)
        val chapter2 = StoryArcEngine.nextEvent(state)!!
        assertEquals("story_border_escalation_2", chapter2.id)
        assertEquals("Envoys at the Table", chapter2.title)
        state = StoryArcEngine.resolve(state, chapter2.id, 1)
        assertEquals(3, state.storyArc.chapter)

        // Chapter 3 — resolution closes the arc and files a SECURITY legacy entry.
        state = StoryArcEngine.onMonth(state)
        val chapter3 = StoryArcEngine.nextEvent(state)!!
        assertEquals("story_border_escalation_3", chapter3.id)
        state = StoryArcEngine.resolve(state, chapter3.id, 0)

        assertNull(state.storyArc.activeArcId)
        assertEquals(0, state.storyArc.chapter)
        assertTrue(state.storyArc.completedArcIds.contains("border_escalation"))
        val storyEntry = state.legacy.entries.last()
        assertTrue("legacy entry: ${storyEntry.title}", storyEntry.title.contains("Border Escalation"))
        assertEquals(LegacyPillar.SECURITY, storyEntry.pillar)
    }

    @Test
    fun completedArcsDoNotReplay() {
        val completed = hostileState().copy(
            storyArc = StoryArcState(completedArcIds = listOf("border_escalation")),
        )
        assertEquals(null, StoryArcEngine.onMonth(completed).storyArc.activeArcId)
    }

    @Test
    fun stateAwareEventsStrengthenWhenTheirSystemIsInPlay() {
        val pool = EventRepository.eventPool
        val opposition = pool.first { it.id == "opposition_protest_wave" }
        val disaster = pool.first { it.id == "disaster_emergency_session" }
        val mandate = pool.first { it.id == "mandate_promises_under_fire" }
        val calm = GameState()

        // Opposition tactics heat the opposition event up.
        val heated = calm.copy(opposition = calm.opposition.copy(noConfidenceHeat = 70f))
        assertTrue(
            EventRepository.weightedEventWeight(opposition, heated) >
                EventRepository.weightedEventWeight(opposition, calm),
        )

        // An active disaster dominates the disaster event.
        val disasterState = calm.copy(
            disaster = calm.disaster.copy(
                active = ActiveDisaster(
                    id = "d1",
                    type = DisasterType.entries.first(),
                    label = "Quake",
                ),
            ),
        )
        assertTrue(
            EventRepository.weightedEventWeight(disaster, disasterState) >
                EventRepository.weightedEventWeight(disaster, calm),
        )

        // Broken mandate promises bring the promises event to life.
        val broken = calm.copy(
            mandate = MandateState(
                lastReview = listOf(
                    MandateCommitmentResult(MandateGoal.FISCAL_RECOVERY, fulfilled = false, review = "missed"),
                ),
            ),
        )
        assertTrue(
            EventRepository.weightedEventWeight(mandate, broken) >
                EventRepository.weightedEventWeight(mandate, calm),
        )
    }

    @Test
    fun stateAwareEventsAreResolvable() {
        listOf("opposition_protest_wave", "disaster_emergency_session", "mandate_promises_under_fire")
            .forEach { id ->
                val event = EventRepository.byId(id)
                assertNotNull("pool must expose $id", event)
                assertTrue("event needs choices", event!!.choices.isNotEmpty())
                event.choices.forEach { choice ->
                    // Applying every choice must stay inside the clamps.
                    val after = choice.consequence.applyTo(GameState())
                    assertTrue(after.vitals.approval in 0f..100f)
                    assertTrue(after.internalSecurity.instabilityScore in 0f..100f)
                    assertTrue(after.cabinet.cohesion in 0f..100f)
                }
            }
    }
}
