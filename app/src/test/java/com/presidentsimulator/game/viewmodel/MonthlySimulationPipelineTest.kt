package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.GameState
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the documented monthly turn order (see [MonthlySimulationPipeline.advance]).
 * Turn order is the #1 balance-regression risk zone: reordering stages silently changes
 * how every subsystem interacts, so this test fails loudly if a stage moves.
 */
class MonthlySimulationPipelineTest {

    private fun recordingPipeline(stages: MutableList<String>) = seededTestPipeline(
        seed = 42L,
        onStage = stages::add,
    )

    @Test
    fun healthyMonthRunsEveryStageInDocumentedOrder() {
        val stages = mutableListOf<String>()
        recordingPipeline(stages).advance(GameState.initial("us"))

        assertEquals(
            listOf(
                "date",
                "procurement",
                "production",
                "fiscal",
                "geopolitics",
                "security",
                "crisis",
                "press",
                "cabinet",
                "opposition",
                "disaster",
                "speech_cooldowns",
                "term",
                "military_industry",
                "victory",
                "victory_milestone",
                "society",
                "laws",
                "policy_impact",
                "trade",
                "governance",
                "demographics",
                "history",
                "legacy",
                "agenda",
                "mandate",
                "news",
                "story_arc",
            ),
            stages,
        )
    }

    @Test
    fun gameOverShortCircuitsAfterSecurityResolution() {
        val stages = mutableListOf<String>()
        val frozen = GameState.initial("us").copy(
            gameOver = GameState.initial("us").gameOver.copy(isGameOver = true),
        )
        recordingPipeline(stages).advance(frozen)

        // The settlement half of the month still resolves (budget, war, security),
        // then everything downstream of the game-over check is skipped.
        assertEquals(
            listOf("date", "procurement", "production", "fiscal", "geopolitics", "security"),
            stages,
        )
    }
}
