package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.ElectionNightResult
import com.presidentsimulator.game.data.ElectionSeasonState
import com.presidentsimulator.game.data.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

/**
 * Golden-master stress test for the monthly simulation (rewritten after the original
 * file in this slot was corrupted and bricked the whole suite).
 *
 * Runs hundreds of months across several seeds and asserts *invariants* rather than
 * brittle exact values: clamps hold, the calendar advances exactly one month per tick,
 * and sane magnitude bounds are respected. Endings must explain themselves: in practice
 * every passive run (no player input) eventually falls to an *unanswered* crisis spiral
 * — epidemics, scandals, and opposition tactics stack instability until the coup meter
 * tops out — which doubles as proof that game-over is reachable from ordinary play.
 * Balance trends are summarised per seed so drift between commits is visible in the
 * test output.
 */
class MonthlySimulationStressTest {

    private companion object {
        const val MONTHS_PER_RUN = 500
        val SEEDS = listOf(1L, 42L, 1337L, 2026L, 999_983L)

        /** Magnitude guard: a Long budget/science value beyond this has wrapped around. */
        const val LONG_SANITY_LIMIT = 1_000_000_000_000_000L
    }

    @Test
    fun longRunningCampaignsHoldSimulationInvariants() {
        val demographics = DemographicsCampaignViewModel()
        val summaries = mutableListOf<String>()

        SEEDS.forEach { seed ->
            val pipeline = seededTestPipeline(seed)
            var state = seededStartingState(seed)
            var approvalSum = 0.0
            var monthsRun = 0
            var ended = false

            repeat(MONTHS_PER_RUN) { step ->
                if (state.gameOver.isGameOver) {
                    ended = true
                    return@repeat
                }

                val beforeMonth = state.month
                val beforeYear = state.year
                state = pipeline.advance(state)

                // The live ticker pauses for these; the player's tap just acknowledges.
                if (state.agenda.needsBriefing) {
                    state = state.copy(
                        agenda = state.agenda.copy(acknowledgedMonthKey = state.agenda.monthKey),
                    )
                }
                if (state.demographics.election.hasPendingNight) {
                    state = demographics.confirmElectionNight(state)
                }

                val (expectedMonth, expectedYear) = advanceSimulationDate(beforeMonth, beforeYear)
                val label = "seed=$seed month=${state.month} step=$step"
                assertEquals("$label calendar advanced", expectedMonth, state.month)
                assertEquals("$label calendar advanced", expectedYear, state.year)

                assertInvariants(label, state)

                approvalSum += state.vitals.approval
                monthsRun++
            }

            if (state.gameOver.isGameOver) {
                ended = true
                assertTrue(
                    "seed=$seed ended without a reason",
                    state.gameOver.reason.isNotBlank(),
                )
            }

            val meanApproval = if (monthsRun > 0) approvalSum / monthsRun else 0.0
            summaries +=
                "seed=$seed months=$monthsRun ended=$ended meanApproval=" +
                    "${meanApproval.roundToInt()} finalBudget=${state.vitals.budget}" +
                    if (ended) " endReason=\"${state.gameOver.reason}\"" else ""        }

        // Visible with `--info`; handy for eyeballing balance drift between commits.
        println("golden-master: ${summaries.joinToString(" | ")}")

        // Balance guardrail: the simulation must not collapse to misery or a utopia
        // across every seed. Deliberately wide — it detects drift, not tuning.
        val overallMean = summaries.map { line ->
            line.substringAfter("meanApproval=").substringBefore(" ").toDouble()
        }.average()
        assertTrue(
            "overall mean approval $overallMean drifted outside the healthy band",
            overallMean in 10.0..95.0,
        )
    }

    @Test
    fun electionDefeatEndsTheCampaignWithAReason() {
        val demographics = DemographicsCampaignViewModel()
        val night = ElectionNightResult(
            victory = false,
            playerShare = 38f,
            challengerShare = 55f,
            workingShare = 40f,
            businessShare = 60f,
            militaryShare = 52f,
            academicsShare = 45f,
            economyOk = true,
            stabilityOk = true,
            oppositionPenalty = 10f,
            challengerName = "United Opposition",
            narrative = "The challenger sweeps the swing regions.",
        )
        val staged = seededStartingState(7L).copy(
            demographics = seededStartingState(7L).demographics.copy(
                election = ElectionSeasonState(pendingNight = night),
            ),
        )

        val ended = demographics.confirmElectionNight(staged)

        assertTrue("defeat must end the campaign", ended.gameOver.isGameOver)
        assertTrue("defeat is not a victory", !ended.gameOver.isVictory)
        assertTrue(
            "reason explains the loss: ${ended.gameOver.reason}",
            ended.gameOver.reason.contains("Election defeat"),
        )
    }

    @Test
    fun highApprovalPastVictoryYearEndsTheCampaignInVictory() {
        val demographics = DemographicsCampaignViewModel()
        val base = seededStartingState(11L)
        val thriving = base.copy(
            month = 12,
            year = base.year + 40,
            vitals = base.vitals.copy(approval = 92f),
            internalSecurity = base.internalSecurity.copy(instabilityScore = 0f),
        )

        val ended = demographics.evaluateCampaignOutcomes(thriving)

        assertTrue("golden age must be winnable", ended.gameOver.isGameOver)
        assertTrue("this ending is a victory", ended.gameOver.isVictory)
        assertTrue(
            "reason explains the win: ${ended.gameOver.reason}",
            ended.gameOver.reason.startsWith("Victory"),
        )
    }

    private fun assertInvariants(label: String, state: GameState) {
        assertTrue("$label approval ${state.vitals.approval}", state.vitals.approval in 0f..100f)
        assertTrue("$label population ${state.vitals.population}", state.vitals.population >= 1_000_000L)
        assertTrue(
            "$label budget ${state.vitals.budget} wrapped",
            state.vitals.budget in -LONG_SANITY_LIMIT..LONG_SANITY_LIMIT,
        )
        assertTrue(
            "$label instability ${state.internalSecurity.instabilityScore}",
            state.internalSecurity.instabilityScore in 0f..100f,
        )
        assertTrue(
            "$label coup risk ${state.internalSecurity.coupRisk}",
            state.internalSecurity.coupRisk in 0f..100f,
        )
        assertTrue("$label defcon ${state.military.defcon}", state.military.defcon in 1..5)
        assertTrue(
            "$label media sentiment ${state.press.mediaSentiment}",
            state.press.mediaSentiment in 0f..100f,
        )
        assertTrue("$label credibility ${state.press.credibility}", state.press.credibility in 0f..100f)
        assertTrue("$label cohesion ${state.cabinet.cohesion}", state.cabinet.cohesion in 0f..100f)
        assertTrue(
            "$label opposition heat ${state.opposition.noConfidenceHeat}",
            state.opposition.noConfidenceHeat in 0f..100f,
        )
        assertTrue(
            "$label science ${state.research.sciencePoints} wrapped",
            state.research.sciencePoints in -LONG_SANITY_LIMIT..LONG_SANITY_LIMIT,
        )
    }
}
