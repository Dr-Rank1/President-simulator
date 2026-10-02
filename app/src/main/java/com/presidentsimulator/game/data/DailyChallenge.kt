package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

/** One recorded daily-challenge result on the local leaderboard. */
@Serializable
data class DailyScore(
    /** UTC calendar day the run belongs to. */
    val epochDay: Long,
    val nationId: String,
    val score: Int,
    val victory: Boolean,
)

/** Launch-screen facts about today's challenge. */
data class DailyChallengeInfo(
    val epochDay: Long,
    val nationId: String,
    val nationName: String,
    val flagEmoji: String,
    val bestScore: Int,
)

/**
 * Daily Seed Challenge: every player gets the same seed and the same nation for a
 * given day, so runs are directly comparable and shareable ("today's Kryos run").
 * Built on the seeded-campaign machinery — a daily run replays exactly.
 */
object DailyChallenge {

    /**
     * Deterministic, non-zero seed shared by everyone for [epochDay].
     * Never 0: zero is reserved as "no seed" in legacy payloads.
     */
    fun seedFor(epochDay: Long): Long {
        // LCG-style step plus xorshift so neighbouring days map to unrelated seeds.
        // (All literals stay below 2^62; Kotlin hex literals cannot set the sign bit.)
        var h = epochDay * 6364136223846793005L + 1442695040888963407L
        h = h xor (h ushr 32)
        h *= 0x5DEECE66DL // 25_214_903_917, odd
        h = h xor (h ushr 28)
        return if (h == 0L || h == Long.MIN_VALUE) 1L else h
    }

    /** The fixed nation of the day, rotated so the challenge varies but stays shared. */
    fun nationIdFor(epochDay: Long): String {
        val nations = PlayableNationCatalog.all()
        require(nations.isNotEmpty()) { "no playable nations" }
        val index = ((epochDay % nations.size) + nations.size).rem(nations.size).toInt()
        return nations[index].id
    }

    fun isDailyRun(state: GameState): Boolean = state.scenario.dailyChallengeEpochDay > 0L

    /** Keeps the best result per day, highest first, capped at [maxEntries]. */
    fun record(
        existing: List<DailyScore>,
        entry: DailyScore,
        maxEntries: Int = 20,
    ): List<DailyScore> =
        (existing.filterNot { it.epochDay == entry.epochDay } + entry)
            .sortedByDescending { it.score }
            .take(maxEntries)

    fun encode(entries: List<DailyScore>): String =
        entries.joinToString(";") { entry ->
            "${entry.epochDay}|${entry.nationId}|${entry.score}|${if (entry.victory) 1 else 0}"
        }

    fun decode(raw: String?): List<DailyScore> =
        raw.orEmpty()
            .split(';')
            .mapNotNull { line ->
                val parts = line.split('|')
                if (parts.size != 4) return@mapNotNull null
                val epochDay = parts[0].toLongOrNull() ?: return@mapNotNull null
                val score = parts[2].toIntOrNull() ?: return@mapNotNull null
                if (epochDay <= 0L) return@mapNotNull null
                DailyScore(
                    epochDay = epochDay,
                    nationId = parts[1],
                    score = score,
                    victory = parts[3] == "1",
                )
            }
}
