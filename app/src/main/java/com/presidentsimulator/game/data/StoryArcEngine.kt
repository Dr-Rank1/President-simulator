package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

@Serializable
data class StoryArcState(
    val activeArcId: String? = null,
    /** The next chapter to present (1–3). */
    val chapter: Int = 0,
    val monthsUntilNextChapter: Int = 0,
    val openingChoice: String = "",
    val completedArcIds: List<String> = emptyList(),
    val lastStoryNote: String = "",
)

/** Saved, branching three-chapter political stories built from existing campaign systems. */
object StoryArcEngine {
    private const val CABINET_SCANDAL = "cabinet_scandal"
    private const val CONFIDENCE_VOTE = "confidence_vote"

    fun onMonth(state: GameState): GameState {
        val current = state.storyArc
        if (current.activeArcId != null) {
            if (current.monthsUntilNextChapter <= 0) return state
            return state.copy(storyArc = current.copy(monthsUntilNextChapter = current.monthsUntilNextChapter - 1))
        }
        val nextArc = when {
            CABINET_SCANDAL !in current.completedArcIds &&
                (state.cabinet.scandalsThisTerm > 0 || state.press.openScandalCount > 0) -> CABINET_SCANDAL
            state.legal.governmentSystem.hasConfidenceVotes &&
                CONFIDENCE_VOTE !in current.completedArcIds &&
                (state.opposition.noConfidenceHeat >= 45f ||
                    (!state.opposition.hasMajority && (state.opposition.mainOpposition?.hostility ?: 0f) >= 75f)) -> CONFIDENCE_VOTE
            else -> null
        } ?: return state

        return state.copy(
            storyArc = current.copy(
                activeArcId = nextArc,
                chapter = 1,
                monthsUntilNextChapter = 0,
                openingChoice = "",
                lastStoryNote = arcTitle(nextArc),
            ),
        )
    }

    fun nextEvent(state: GameState): GameEvent? {
        val arc = state.storyArc
        if (arc.activeArcId == null || arc.monthsUntilNextChapter > 0) return null
        return event(arc.activeArcId, arc.chapter, arc.openingChoice)
    }

    fun eventById(id: String): GameEvent? {
        val match = Regex("^story_(cabinet_scandal|confidence_vote)_([1-3])$").matchEntire(id) ?: return null
        return event(match.groupValues[1], match.groupValues[2].toInt(), "")
    }

    fun isStoryEvent(id: String): Boolean = id.startsWith("story_")

    fun resolve(state: GameState, eventId: String, choiceIndex: Int): GameState {
        val arc = state.storyArc
        val expectedId = arc.activeArcId?.let { "story_${it}_${arc.chapter}" }
        if (eventId != expectedId) return state

        if (arc.chapter < 3) {
            val openingChoice = if (arc.chapter == 1) {
                if (choiceIndex == 0) "open" else "defensive"
            } else arc.openingChoice
            val updated = arc.copy(
                chapter = arc.chapter + 1,
                monthsUntilNextChapter = 1,
                openingChoice = openingChoice,
                lastStoryNote = when (arc.activeArcId) {
                    CABINET_SCANDAL -> if (openingChoice == "open") "The inquiry widens" else "The cover-up draws scrutiny"
                    else -> if (openingChoice == "open") "Negotiations continue" else "The whip count tightens"
                },
            )
            return state.copy(storyArc = updated)
        }

        val arcId = arc.activeArcId
        val title = arcTitle(arcId)
        val successful = when (arcId) {
            CABINET_SCANDAL -> state.press.credibility >= 48f && state.cabinet.cohesion >= 40f
            else -> state.opposition.noConfidenceHeat < 45f && state.vitals.approval >= 45f
        }
        val entry = LegacyEntry(
            id = "story_${arcId}_${state.scenario.challengeSeed}",
            year = state.year,
            month = state.month,
            title = if (successful) "$title contained" else "$title deepened",
            detail = if (successful) "Your choices shaped the outcome of a three-part national story." else "The story closed, but its consequences remain in the national record.",
            pillar = LegacyPillar.MANDATE,
            tone = if (successful) LegacyTone.MILESTONE else LegacyTone.TURNING_POINT,
            scoreDelta = if (successful) 5 else -3,
        )
        return state.copy(
            storyArc = arc.copy(
                activeArcId = null,
                chapter = 0,
                monthsUntilNextChapter = 0,
                completedArcIds = (arc.completedArcIds + arcId).distinct(),
                lastStoryNote = entry.title,
            ),
            legacy = state.legacy.copy(
                scores = state.legacy.scores.adjust(LegacyPillar.MANDATE, if (successful) 4 else -2),
                entries = (state.legacy.entries + entry).takeLast(40),
                lastLegacyNote = entry.title,
            ),
        )
    }

    private fun arcTitle(id: String): String = when (id) {
        CABINET_SCANDAL -> "Cabinet Leak"
        CONFIDENCE_VOTE -> "Confidence Crisis"
        else -> "National Story"
    }

    private fun event(arcId: String, chapter: Int, branch: String): GameEvent? = when (arcId to chapter) {
        CABINET_SCANDAL to 1 -> GameEvent(
            id = "story_${CABINET_SCANDAL}_1",
            title = "A Source Steps Forward",
            description = "A cabinet insider offers documents that could expose a procurement scandal. The opposition is preparing a hearing, and the press is waiting for your response.",
            choices = listOf(
                EventChoice("Open an independent inquiry", EventConsequence(budgetChange = -1_500_000_000L, approvalChange = 3f, mediaSentimentChange = 5f, pressCredibilityChange = 10f, oppositionHeatChange = -6f, cabinetCohesionChange = -3f)),
                EventChoice("Defend the cabinet and challenge the source", EventConsequence(approvalChange = -2f, mediaSentimentChange = -5f, pressCredibilityChange = -9f, oppositionHeatChange = 5f, cabinetCohesionChange = 3f)),
            ),
        )
        CABINET_SCANDAL to 2 -> GameEvent(
            id = "story_${CABINET_SCANDAL}_2",
            title = if (branch == "open") "The Inquiry Widens" else "The Cover-up Draws Scrutiny",
            description = if (branch == "open") "Investigators have found irregular contracts. A public hearing could restore trust, but it may split your cabinet." else "New documents contradict the official denial. Your ministers want a united front; the opposition demands testimony under oath.",
            choices = listOf(
                EventChoice("Testify and release the records", EventConsequence(budgetChange = -1_000_000_000L, approvalChange = 2f, mediaSentimentChange = 4f, pressCredibilityChange = 8f, oppositionHeatChange = -5f, cabinetCohesionChange = -2f)),
                EventChoice("Limit the hearing to a closed committee", EventConsequence(approvalChange = -3f, mediaSentimentChange = -4f, pressCredibilityChange = -6f, oppositionHeatChange = 6f, cabinetCohesionChange = 2f)),
            ),
        )
        CABINET_SCANDAL to 3 -> GameEvent(
            id = "story_${CABINET_SCANDAL}_3",
            title = "The Cabinet's Future",
            description = "The inquiry has reached its final report. Your ministers, the opposition leader, and the public are watching how you close the scandal.",
            choices = listOf(
                EventChoice("Accept the findings and remove those responsible", EventConsequence(approvalChange = 4f, mediaSentimentChange = 5f, pressCredibilityChange = 6f, oppositionHeatChange = -8f, cabinetCohesionChange = -5f)),
                EventChoice("Keep the team and ask voters to move on", EventConsequence(approvalChange = -4f, mediaSentimentChange = -5f, pressCredibilityChange = -8f, oppositionHeatChange = 8f, cabinetCohesionChange = 4f)),
            ),
        )
        CONFIDENCE_VOTE to 1 -> GameEvent(
            id = "story_${CONFIDENCE_VOTE}_1",
            title = "A Motion of No Confidence",
            description = "The opposition leader has tabled a confidence motion. Coalition partners are asking what they will get in return for keeping your government in office.",
            choices = listOf(
                EventChoice("Offer a cross-party policy compact", EventConsequence(budgetChange = -2_000_000_000L, approvalChange = 2f, oppositionHeatChange = -10f, cabinetCohesionChange = -3f)),
                EventChoice("Call the motion a political stunt", EventConsequence(approvalChange = 1f, mediaSentimentChange = 2f, oppositionHeatChange = 8f)),
            ),
        )
        CONFIDENCE_VOTE to 2 -> GameEvent(
            id = "story_${CONFIDENCE_VOTE}_2",
            title = if (branch == "open") "The Coalition Compact" else "The Whip Count Tightens",
            description = if (branch == "open") "Negotiators have a draft agreement, but your cabinet says the concessions go too far." else "Two backbenchers may cross the floor. Your chief whip needs a clear offer before the vote is called.",
            choices = listOf(
                EventChoice("Accept a limited reform package", EventConsequence(budgetChange = -1_000_000_000L, approvalChange = 3f, oppositionHeatChange = -7f, cabinetCohesionChange = -4f)),
                EventChoice("Hold the line and rally your own party", EventConsequence(approvalChange = -1f, mediaSentimentChange = 2f, oppositionHeatChange = 5f, cabinetCohesionChange = 4f)),
            ),
        )
        CONFIDENCE_VOTE to 3 -> GameEvent(
            id = "story_${CONFIDENCE_VOTE}_3",
            title = "Confidence Vote",
            description = "Parliament is ready to vote. The result will decide whether your government can continue on its current mandate.",
            choices = listOf(
                EventChoice("Face the vote in public", EventConsequence(approvalChange = 3f, mediaSentimentChange = 4f, oppositionHeatChange = -9f, cabinetCohesionChange = 3f)),
                EventChoice("Delay the vote and negotiate overnight", EventConsequence(budgetChange = -1_500_000_000L, approvalChange = -2f, oppositionHeatChange = -4f, cabinetCohesionChange = -2f)),
            ),
        )
        else -> null
    }
}
