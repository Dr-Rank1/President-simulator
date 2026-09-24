package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.roundToInt

@Serializable
data class PolicyObservation(
    val lawId: String,
    val startedYear: Int,
    val startedMonth: Int,
    val approvalAtStart: Float,
    val monthlyBalanceAtStart: Long,
    val productionAtStart: Float,
    val workersAtStart: Float,
    val businessAtStart: Float,
    val monthsObserved: Int = 0,
)

@Serializable
data class PolicyImpactReport(
    val lawId: String,
    val lawName: String,
    val year: Int,
    val month: Int,
    val monthsObserved: Int,
    val approvalChange: Float,
    val monthlyBalanceChange: Long,
    val productionChange: Float,
    val workersChange: Float,
    val businessChange: Float,
) {
    fun summary(): String = buildString {
        append("After $monthsObserved month(s): approval ${approvalChange.signedPoints()}, ")
        append("monthly balance ${monthlyBalanceChange.signedBudget()}, production ${productionChange.signedPercent()}.")
        if (abs(workersChange) >= 1f || abs(businessChange) >= 1f) {
            append(" Worker support ${workersChange.signedPoints()}; business support ${businessChange.signedPoints()}.")
        }
    }
}

@Serializable
data class PolicyInsightsState(
    val observations: List<PolicyObservation> = emptyList(),
    val reports: List<PolicyImpactReport> = emptyList(),
)

object PolicyImpactEngine {
    private val reviewMonths = setOf(3, 6, 12)

    fun begin(state: GameState, lawId: String): GameState {
        if (state.legal.policyInsights.observations.any { it.lawId == lawId }) return state
        val law = LawCatalog.byId(lawId) ?: return state
        val observation = PolicyObservation(
            lawId = lawId,
            startedYear = state.year,
            startedMonth = state.month,
            approvalAtStart = state.vitals.approval,
            monthlyBalanceAtStart = state.netIncome,
            productionAtStart = state.effectiveProductionMultiplier,
            workersAtStart = state.demographics.workingClass,
            businessAtStart = state.demographics.businessElite,
        )
        return state.copy(legal = state.legal.copy(
            policyInsights = state.legal.policyInsights.copy(
                observations = state.legal.policyInsights.observations + observation,
            ),
        ))
    }

    fun end(state: GameState, lawId: String): GameState {
        val observation = state.legal.policyInsights.observations.firstOrNull { it.lawId == lawId } ?: return state
        val report = reportFor(state, observation)
        return state.copy(legal = state.legal.copy(
            policyInsights = state.legal.policyInsights.copy(
                observations = state.legal.policyInsights.observations.filterNot { it.lawId == lawId },
                reports = if (observation.monthsObserved > 0 && observation.monthsObserved !in reviewMonths) (state.legal.policyInsights.reports + report).takeLast(8) else state.legal.policyInsights.reports,
            ),
        ))
    }

    fun processMonth(state: GameState): GameState {
        val legal = state.legal
        val activeIds = legal.activeLawIds.toSet()
        val active = legal.policyInsights.observations.filter { it.lawId in activeIds }
        val removed = legal.policyInsights.observations.filter { it.lawId !in activeIds }
        val reports = legal.policyInsights.reports.toMutableList()
        val updated = active.map { observation ->
            if (observation.monthsObserved >= 12 || (observation.startedYear == state.year && observation.startedMonth == state.month)) return@map observation
            val months = observation.monthsObserved + 1
            if (months in reviewMonths) reports += reportFor(state, observation.copy(monthsObserved = months))
            observation.copy(monthsObserved = months)
        }
        val trackedIds = updated.map { it.lawId }.toSet()
        val newObservations = activeIds.filterNot { it in trackedIds }.mapNotNull { lawId ->
            if (LawCatalog.byId(lawId) == null) return@mapNotNull null
            PolicyObservation(lawId, state.year, state.month, state.vitals.approval, state.netIncome,
                state.effectiveProductionMultiplier, state.demographics.workingClass, state.demographics.businessElite)
        }
        removed.forEach { observation ->
            if (observation.monthsObserved > 0 && observation.monthsObserved !in reviewMonths) reports += reportFor(state, observation)
        }
        return state.copy(legal = legal.copy(policyInsights = PolicyInsightsState(
            observations = (updated + newObservations).distinctBy { it.lawId },
            reports = reports.takeLast(8),
        )))
    }

    private fun reportFor(state: GameState, observation: PolicyObservation): PolicyImpactReport =
        PolicyImpactReport(
            lawId = observation.lawId,
            lawName = LawCatalog.byId(observation.lawId)?.name ?: observation.lawId,
            year = state.year,
            month = state.month,
            monthsObserved = observation.monthsObserved.coerceAtLeast(1),
            approvalChange = state.vitals.approval - observation.approvalAtStart,
            monthlyBalanceChange = state.netIncome - observation.monthlyBalanceAtStart,
            productionChange = state.effectiveProductionMultiplier - observation.productionAtStart,
            workersChange = state.demographics.workingClass - observation.workersAtStart,
            businessChange = state.demographics.businessElite - observation.businessAtStart,
        )
}

@Serializable
enum class MandateGoal(val title: String, val description: String) {
    FISCAL_RECOVERY("Restore the budget", "Finish the term with a monthly operating surplus."),
    FOOD_SECURITY("Protect food security", "Avoid food shortages for three consecutive months."),
    PUBLIC_CONFIDENCE("Earn public confidence", "Reach at least 60% approval."),
    NATIONAL_STABILITY("Keep the peace", "Bring coup risk below 30%."),
}

@Serializable
data class MandateCommitment(val goal: MandateGoal, val madeYear: Int, val madeMonth: Int, val progress: Int = 0)

@Serializable
data class MandateCommitmentResult(val goal: MandateGoal, val fulfilled: Boolean, val review: String)

@Serializable
data class MandateState(
    val commitments: List<MandateCommitment> = emptyList(),
    val lastReview: List<MandateCommitmentResult> = emptyList(),
)

object MandateEngine {
    fun makeCommitment(state: GameState, goal: MandateGoal): GameState {
        if (state.mandate.commitments.any { it.goal == goal } || state.mandate.commitments.size >= 3) return state
        val commitment = MandateCommitment(goal, state.year, state.month)
        return state.copy(mandate = state.mandate.copy(commitments = state.mandate.commitments + commitment))
    }

    fun processMonth(state: GameState): GameState = state.copy(mandate = state.mandate.copy(
        commitments = state.mandate.commitments.map { commitment ->
            if (commitment.goal != MandateGoal.FOOD_SECURITY) commitment
            else commitment.copy(progress = if (state.production.foodShortage) 0 else (commitment.progress + 1).coerceAtMost(3))
        },
    ))

    fun closeTerm(state: GameState): GameState {
        if (state.mandate.commitments.isEmpty()) return state
        val results = state.mandate.commitments.map { commitment ->
            val fulfilled = isFulfilled(state, commitment)
            MandateCommitmentResult(commitment.goal, fulfilled, if (fulfilled) "Delivered" else currentSignal(state, commitment))
        }
        val entries = results.map { result ->
            LegacyEntry(
                id = "mandate_${result.goal.name.lowercase()}_${state.year}_${state.month}",
                year = state.year,
                month = state.month,
                title = if (result.fulfilled) "Promise kept: ${result.goal.title}" else "Promise broken: ${result.goal.title}",
                detail = result.review,
                pillar = LegacyPillar.MANDATE,
                tone = if (result.fulfilled) LegacyTone.MILESTONE else LegacyTone.STAIN,
                scoreDelta = if (result.fulfilled) 3 else -3,
            )
        }
        val mandateScoreDelta = results.fold(0) { total, result -> total + if (result.fulfilled) 3 else -3 }
        return state.copy(
            mandate = state.mandate.copy(commitments = emptyList(), lastReview = results),
            legacy = state.legacy.copy(
                scores = state.legacy.scores.adjust(LegacyPillar.MANDATE, mandateScoreDelta),
                entries = (state.legacy.entries + entries).takeLast(40),
                lastLegacyNote = entries.lastOrNull()?.title ?: state.legacy.lastLegacyNote,
            ),
        )
    }

    fun isFulfilled(state: GameState, commitment: MandateCommitment): Boolean = when (commitment.goal) {
        MandateGoal.FISCAL_RECOVERY -> state.netIncome >= 0L
        MandateGoal.FOOD_SECURITY -> commitment.progress >= 3
        MandateGoal.PUBLIC_CONFIDENCE -> state.vitals.approval >= 60f
        MandateGoal.NATIONAL_STABILITY -> state.internalSecurity.coupRisk < 30f
    }

    fun currentSignal(state: GameState, commitment: MandateCommitment): String = when (commitment.goal) {
        MandateGoal.FISCAL_RECOVERY -> "${state.netIncome.signedBudget()} monthly balance"
        MandateGoal.FOOD_SECURITY -> "${commitment.progress}/3 secure months"
        MandateGoal.PUBLIC_CONFIDENCE -> "${state.vitals.approval.roundToInt()}% approval · target 60%"
        MandateGoal.NATIONAL_STABILITY -> "${state.internalSecurity.coupRisk.roundToInt()}% coup risk · target below 30%"
    }
}

private fun Float.signedPoints(): String = "${if (this >= 0f) "+" else ""}${this.roundToInt()} pts"
private fun Float.signedPercent(): String = "${if (this >= 0f) "+" else ""}${(this * 100).roundToInt()}%"
private fun Long.signedBudget(): String {
    val amount = kotlin.math.abs(this)
    val sign = if (this >= 0) "+" else "−"
    return if (amount >= 1_000_000_000L) {
        val whole = amount / 1_000_000_000L
        val tenth = (amount % 1_000_000_000L) / 100_000_000L
        "${sign}\$$whole.${tenth}B"
    } else {
        "${sign}\$${amount / 1_000_000L}M"
    }
}
