package com.presidentsimulator.game.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyChallengeTest {

    @Test
    fun seedIsStableAndNeverZero() {
        (0L..400L).forEach { day ->
            val seed = DailyChallenge.seedFor(day)
            assertEquals("seed must be replay-stable for day $day", seed, DailyChallenge.seedFor(day))
            assertTrue("seed for day $day must be non-zero", seed != 0L)
        }
    }

    @Test
    fun neighbouringDaysGetDifferentSeeds() {
        val seeds = (0L..364L).map { DailyChallenge.seedFor(it) }
        assertEquals("365 distinct daily seeds", seeds.size, seeds.distinct().size)
        assertNotEquals(DailyChallenge.seedFor(1L), DailyChallenge.seedFor(2L))
    }

    @Test
    fun nationIsFixedPerDayAndAlwaysPlayable() {
        (0L..99L).forEach { day ->
            val nationId = DailyChallenge.nationIdFor(day)
            assertEquals("same day, same nation", nationId, DailyChallenge.nationIdFor(day))
            assertNotNull("nation $nationId for day $day must exist", PlayableNationCatalog.byId(nationId))
        }
    }

    @Test
    fun recordKeepsBestResultPerDaySortedAndCapped() {
        val day = 100L
        var board = emptyList<DailyScore>()
        board = DailyChallenge.record(board, DailyScore(day, "us", 40, victory = false))
        board = DailyChallenge.record(board, DailyScore(day, "us", 65, victory = true))
        assertEquals("better attempt replaces the worse one", 1, board.size)
        assertEquals(65, board.single().score)

        (1L..25L).forEach { offset ->
            board = DailyChallenge.record(
                board,
                DailyScore(epochDay = 500L + offset, nationId = "de", score = (offset * 3).toInt(), victory = false),
            )
        }
        assertEquals("leaderboard is capped", 20, board.size)
        assertEquals("sorted by score", board.map { it.score }, board.map { it.score }.sortedDescending())
    }

    @Test
    fun scoreCodecRoundTrips() {
        val entries = listOf(
            DailyScore(epochDay = 20_000L, nationId = "us", score = 120, victory = true),
            DailyScore(epochDay = 20_001L, nationId = "kryos", score = 75, victory = false),
        )
        assertEquals(entries, DailyChallenge.decode(DailyChallenge.encode(entries)))
        assertTrue("blank prefs decode to nothing", DailyChallenge.decode(null).isEmpty())
        assertTrue("garbage decodes to nothing", DailyChallenge.decode("junk;;||;also junk").isEmpty())
    }

    @Test
    fun campaignScoreAppliesTheChallengeMultiplier() {
        val base = GameState.initial("us")
        val once = LegacyLedger.campaignScore(base)
        val doubleMultiplier = base.copy(scenario = base.scenario.copy(scoreMultiplier = 2f))

        assertEquals(once * 2, LegacyLedger.campaignScore(doubleMultiplier))
        assertEquals(
            "legacy pillars are the base of the score",
            (base.legacy.scores.overall * 1f).toInt(),
            once,
        )
    }
}
