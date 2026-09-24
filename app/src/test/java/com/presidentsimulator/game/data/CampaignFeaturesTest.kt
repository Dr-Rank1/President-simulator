package com.presidentsimulator.game.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CampaignFeaturesTest {
    @Test
    fun cabinetStoryProgressesAcrossThreeChoicesAndWritesLegacy() {
        var state = GameState(cabinet = CabinetState(scandalsThisTerm = 1))
        state = StoryArcEngine.onMonth(state)

        repeat(3) { chapterIndex ->
            val event = StoryArcEngine.nextEvent(state)
            assertNotNull(event)
            assertEquals("story_cabinet_scandal_${chapterIndex + 1}", event!!.id)
            assertEquals(2, event.choices.size)
            state = StoryArcEngine.resolve(state, event.id, 0)
            if (chapterIndex < 2) {
                assertNull(StoryArcEngine.nextEvent(state))
                state = StoryArcEngine.onMonth(state)
            }
        }

        assertNull(state.storyArc.activeArcId)
        assertTrue("cabinet_scandal" in state.storyArc.completedArcIds)
        assertEquals(1, state.legacy.entries.size)
    }

    @Test
    fun selectedChallengeAppliesItsModifierAndMultiplier() {
        val initial = GameState.initial()
        val challenged = ScenarioCatalog.apply(initial, "standard", seed = 7, challengeId = "hostile_press")

        assertEquals("hostile_press", challenged.scenario.challengeId)
        assertEquals(1.2f, challenged.scenario.scoreMultiplier)
        assertTrue(challenged.press.mediaSentiment < initial.press.mediaSentiment)
        assertTrue(challenged.press.credibility < initial.press.credibility)
    }

    @Test
    fun unknownChallengeFallsBackToClassicRules() {
        val state = ScenarioCatalog.apply(GameState.initial(), "standard", seed = 3, challengeId = "missing")

        assertEquals("standard", state.scenario.challengeId)
        assertEquals(1f, state.scenario.scoreMultiplier)
    }
}
