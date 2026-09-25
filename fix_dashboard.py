with open("app/src/main/java/com/presidentsimulator/game/ui/screens/MainDashboardScreen.kt", "r") as f:
    content = f.read()

import_str = "import com.presidentsimulator.game.data.ScenarioCatalog"
if import_str not in content:
    content = content.replace("import com.presidentsimulator.game.ui.components.formatCompactMoney", 
    "import com.presidentsimulator.game.ui.components.formatCompactMoney\\n" + import_str)
else:
    content = content.replace("import com.presidentsimulator.game.ui.components.formatCompactMoney", 
    "import com.presidentsimulator.game.ui.components.formatCompactMoney")

old_objectives = """            val objectives = state.scenario.victoryConditions(state)
            objectives.forEach { obj ->"""

new_objectives = """            val objectives = campaignObjectives(state)
            objectives.forEach { obj ->"""

content = content.replace(old_objectives, new_objectives)

if "fun campaignObjectives" not in content:
    content += """

private fun campaignObjectives(state: GameState): List<Pair<String, Boolean>> {
    val labels = ScenarioCatalog.byId(state.scenario.scenarioId).objectives
    val averageRelations = state.diplomacy.rivals.map { it.relationshipScore }.average().takeIf { it.isFinite() } ?: 0.0
    val stable = state.internalSecurity.instabilityScore < 30f
    val wonElection = state.legacy.electionsWon > 0
    val complete = when (state.scenario.scenarioId) {
        "peaceful_opening" -> listOf(state.netIncome >= 0L, state.vitals.approval >= 55f, !state.production.foodShortage)
        "powder_keg" -> listOf(state.vitals.budget >= 3_000_000_000L, averageRelations >= 0.0, wonElection)
        "empty_granaries" -> listOf(!state.production.foodShortage, state.vitals.approval >= 50f, state.economy.farms >= 12)
        "palace_intrigue" -> listOf(state.cabinet.cohesion >= 60f, state.press.credibility >= 55f, state.internalSecurity.coupRisk < 30f)
        "iron_curtain" -> listOf(averageRelations >= 0.0, stable, state.scenario.victoryYearOverride?.let { state.year >= it } ?: false)
        "reform_or_die" -> listOf(state.opposition.noConfidenceHeat < 25f, state.demographics.oppositionMomentum < 25f, wonElection)
        else -> listOf(state.netIncome >= 0L, state.vitals.approval >= 55f && stable, state.legacy.scores.overall >= 70)
    }
    return labels.mapIndexed { index, label -> label to (complete.getOrNull(index) ?: false) }
}
"""

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/MainDashboardScreen.kt", "w") as f:
    f.write(content)
