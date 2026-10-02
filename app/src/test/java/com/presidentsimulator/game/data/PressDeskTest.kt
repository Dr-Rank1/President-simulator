package com.presidentsimulator.game.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PressDeskTest {

    private fun stateWith(
        budget: Long = 100_000_000_000L,
        approval: Float = 50f,
        spinCooldown: Int = 0,
        suppressCooldown: Int = 0,
        headlines: List<PressHeadline> = listOf(negativeHeadline()),
    ): GameState {
        val base = GameState(vitals = VitalsState(budget = budget, approval = approval))
        return base.copy(
            press = base.press.copy(
                headlines = headlines,
                spinCooldownMonths = spinCooldown,
                suppressCooldownMonths = suppressCooldown,
            ),
        )
    }

    private fun negativeHeadline(id: String = "headline-1") = PressHeadline(
        id = id,
        title = "Ministry accused of cover-up",
        lede = "Opposition demands answers on the missing funds.",
        tone = HeadlineTone.NEGATIVE,
        year = 2026,
        month = 1,
        outlet = PressOutlet.TABLOID,
        topic = PressTopic.SCANDAL,
    )

    @Test
    fun spinCostsMoneyRunsCooldownAndHandlesTheStory() {
        val before = stateWith()
        val spun = PressDesk.spinHeadline(before, "headline-1", Random(42))

        assertEquals(before.vitals.budget - PressDesk.SPIN_COST, spun.vitals.budget)
        assertEquals(PressDesk.SPIN_COOLDOWN, spun.press.spinCooldownMonths)
        assertEquals(1, spun.press.storiesSpun)
        val target = spun.press.headlines.first { it.id == "headline-1" }
        assertTrue("spin must resolve the story", target.handled)
        assertTrue("spun story is never still negative-positive", target.tone != HeadlineTone.POSITIVE)
        assertTrue("approval stays clamped", spun.vitals.approval in 0f..100f)
        assertTrue("sentiment stays clamped", spun.press.mediaSentiment in 0f..100f)
        assertTrue("credibility floor respected", spun.press.credibility >= 10f)
    }

    @Test
    fun spinIsRefusedWithoutBudgetOrDuringCooldown() {
        val broke = stateWith(budget = PressDesk.SPIN_COST - 1)
        assertEquals(broke, PressDesk.spinHeadline(broke, "headline-1", Random(1)))

        val cooling = stateWith(spinCooldown = 1)
        assertEquals(cooling, PressDesk.spinHeadline(cooling, "headline-1", Random(1)))

        val unknownStory = stateWith()
        assertEquals(
            unknownStory,
            PressDesk.spinHeadline(unknownStory, "no-such-headline", Random(1)),
        )
    }

    @Test
    fun suppressWithholdsTheStoryAndRaisesLeakRisk() {
        val before = stateWith()
        val after = PressDesk.suppressHeadline(before, "headline-1")

        assertEquals(before.vitals.budget - PressDesk.SUPPRESS_COST, after.vitals.budget)
        assertEquals(PressDesk.SUPPRESS_COOLDOWN, after.press.suppressCooldownMonths)
        val target = after.press.headlines.first { it.id == "headline-1" }
        assertTrue(target.handled)
        assertEquals("Story withheld", target.title)
        assertTrue(
            "suppression must raise leak risk (${after.press.leakRisk})",
            after.press.leakRisk > before.press.leakRisk,
        )
    }

    @Test
    fun suppressIsRefusedDuringCooldownOrWithoutBudget() {
        val cooling = stateWith(suppressCooldown = 2)
        assertEquals(cooling, PressDesk.suppressHeadline(cooling, "headline-1"))

        val broke = stateWith(budget = PressDesk.SUPPRESS_COST - 1)
        assertEquals(broke, PressDesk.suppressHeadline(broke, "headline-1"))
    }

    @Test
    fun monthlyCycleKeepsPressAndApprovalClamped() {
        var state = stateWith(approval = 0f)
        val random = Random(2026L)
        repeat(60) {
            state = PressDesk.processMonth(state, random)
            assertTrue("sentiment ${state.press.mediaSentiment}", state.press.mediaSentiment in 0f..100f)
            assertTrue("credibility ${state.press.credibility}", state.press.credibility in 10f..100f)
            assertTrue("freedom ${state.press.pressFreedom}", state.press.pressFreedom in 15f..95f)
            assertTrue("approval ${state.vitals.approval}", state.vitals.approval in 0f..100f)
            assertTrue("instability", state.internalSecurity.instabilityScore in 0f..100f)
            assertTrue(
                "headline cap ${state.press.headlines.size}",
                state.press.headlines.size <= PressDesk.MAX_HEADLINES,
            )
        }
    }
}
